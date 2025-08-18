package com.example.apibackend.payment;

import com.example.apibackend.cart.CartRepository;
import com.example.apibackend.cart.Cart;
import com.example.apibackend.cart.CartItem;
import com.example.apibackend.course.Course;
import com.example.apibackend.course.CourseRepository;
import com.example.apibackend.user.User;
import com.example.apibackend.user.UserRepository;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.exception.StripeException;
import com.stripe.net.RequestOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository; // Add CartRepository

    /**
     * Creates a new PENDING payment or returns an existing one for idempotency.
     * This prevents duplicate charges if the user retries rapidly.
     *
     * @param userId   The authenticated user's ID (from JWT)
     * @param courseId The course to purchase
     * @return CheckoutResponseDTO with payment details
     */

    @Transactional
    public CheckoutResponseDTO createOrGetPendingPayment(Long userId, Long courseId) {
        Optional<Payment> existing = paymentRepository.findTopByUserIdAndCourseIdAndStatusOrderByCreatedAtDesc(
                userId, courseId, Payment.PaymentStatus.PENDING);
        if (existing.isPresent()) {
            Payment payment = existing.get();
            // If Stripe PaymentIntent already created, return real clientSecret
            String clientSecret = payment.getGatewayTxnId() != null ? fetchStripeClientSecret(payment.getGatewayTxnId()) : "cs_test_" + payment.getId();
            return new CheckoutResponseDTO(payment.getId(), clientSecret, payment.getAmountCents(), payment.getCurrency(), payment.getStatus().name());
        }

        // Fetch course and user
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Create new Payment row with status PENDING
        Payment payment = new Payment();
        payment.setUser(user);
        payment.setCourse(course);
        payment.setAmountCents(course.getPriceCents());
        payment.setCurrency(course.getCurrency() != null ? course.getCurrency() : "USD");
        payment.setStatus(Payment.PaymentStatus.PENDING);
        payment.setGatewayTxnId(null); // Will be filled after Stripe intent creation
        payment = paymentRepository.save(payment);

        // Create Stripe PaymentIntent for this course purchase
        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(Long.valueOf(payment.getAmountCents()))
                    .setCurrency(payment.getCurrency().toLowerCase())
                    .putMetadata("paymentId", payment.getId().toString())
                    .putMetadata("userId", userId.toString())
                    .putMetadata("courseId", courseId.toString())
                    .setDescription("Course purchase: " + course.getTitle())
                    .build();
            RequestOptions requestOptions = RequestOptions.builder()
                    .setIdempotencyKey("payment-" + payment.getId())
                    .build();
            PaymentIntent intent = PaymentIntent.create(params, requestOptions);
            payment.setGatewayTxnId(intent.getId()); // Persist Stripe PaymentIntent ID
            paymentRepository.save(payment);
            // MVP: One PaymentIntent per course. In future, use payment_items for single cart charge.
            return new CheckoutResponseDTO(payment.getId(), intent.getClientSecret(), payment.getAmountCents(), payment.getCurrency(), payment.getStatus().name());
        } catch (StripeException e) {
            throw new RuntimeException("Stripe PaymentIntent creation failed", e);
        }
    }

    /**
     * Creates or reuses PENDING payments for all published courses in user's cart.
     * Returns array of payment DTOs for each course. Multiple payments are acceptable for MVP.
     * In future, a payment_items table would allow a single charge for all cart items.
     */
    @Transactional
    public CartCheckoutResponseDTO createOrGetPendingPaymentsForCart(Long userId) {
        // Find user's cart
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found"));
        List<CartCheckoutResponseDTO.CartPaymentDTO> paymentDTOs = new java.util.ArrayList<>();
        for (CartItem item : cart.getItems()) {
            // Fetch course and validate published
            Course course = courseRepository.findById(item.getCourseId())
                    .orElse(null);
            if (course == null || !Boolean.TRUE.equals(course.getIsActive())) {
                // Skip unpublished or missing courses
                continue;
            }
            // Idempotency: reuse existing PENDING payment if present
            Optional<Payment> existing = paymentRepository.findTopByUserIdAndCourseIdAndStatusOrderByCreatedAtDesc(
                    userId, course.getId(), Payment.PaymentStatus.PENDING);
            Payment payment;
            String clientSecret;
            if (existing.isPresent()) {
                payment = existing.get();
                clientSecret = payment.getGatewayTxnId() != null ? fetchStripeClientSecret(payment.getGatewayTxnId()) : "cs_test_" + payment.getId();
            } else {
                // Create new payment intent
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new IllegalArgumentException("User not found"));
                payment = new Payment();
                payment.setUser(user);
                payment.setCourse(course);
                payment.setAmountCents(course.getPriceCents());
                payment.setCurrency(course.getCurrency() != null ? course.getCurrency() : "USD");
                payment.setStatus(Payment.PaymentStatus.PENDING);
                payment.setGatewayTxnId(null);
                payment = paymentRepository.save(payment);
                try {
                    PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                            .setAmount(Long.valueOf(payment.getAmountCents()))
                            .setCurrency(payment.getCurrency().toLowerCase())
                            .putMetadata("paymentId", payment.getId().toString())
                            .putMetadata("userId", userId.toString())
                            .putMetadata("courseId", course.getId().toString())
                            .setDescription("Course purchase: " + course.getTitle())
                            .build();
                    RequestOptions requestOptions = RequestOptions.builder()
                            .setIdempotencyKey("payment-" + payment.getId())
                            .build();
                    PaymentIntent intent = PaymentIntent.create(params, requestOptions);
                    payment.setGatewayTxnId(intent.getId());
                    paymentRepository.save(payment);
                    clientSecret = intent.getClientSecret();
                } catch (StripeException e) {
                    throw new RuntimeException("Stripe PaymentIntent creation failed", e);
                }
            }
            paymentDTOs.add(new CartCheckoutResponseDTO.CartPaymentDTO(
                    payment.getId(), course.getId(), payment.getAmountCents(), payment.getCurrency(), payment.getStatus().name(), clientSecret
            ));
        }
        // NOTE: Multiple payments are acceptable for MVP. In future, use payment_items for single charge.
        return new CartCheckoutResponseDTO(paymentDTOs);
    }

    // Helper to fetch clientSecret from Stripe for an existing PaymentIntent
    private String fetchStripeClientSecret(String paymentIntentId) {
        try {
            PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);
            return intent.getClientSecret();
        } catch (StripeException e) {
            throw new RuntimeException("Failed to fetch Stripe clientSecret", e);
        }
    }

    // Converts Payment entity to CheckoutResponseDTO
    private CheckoutResponseDTO toCheckoutDTO(Payment payment) {
        // In real gateway integration, clientSecret would come from Stripe/Razorpay
        String clientSecret = "cs_test_" + (payment.getId() != null ? payment.getId() : UUID.randomUUID());
        return new CheckoutResponseDTO(payment.getId(), clientSecret, payment.getAmountCents(), payment.getCurrency(), payment.getStatus().name());
    }
}
