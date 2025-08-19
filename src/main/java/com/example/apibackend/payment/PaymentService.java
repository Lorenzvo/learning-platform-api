package com.example.apibackend.payment;

import com.example.apibackend.cart.CartRepository;
import com.example.apibackend.cart.Cart;
import com.example.apibackend.cart.CartItem;
import com.example.apibackend.course.Course;
import com.example.apibackend.course.CourseRepository;
import com.example.apibackend.user.User;
import com.example.apibackend.user.UserRepository;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.exception.StripeException;
import com.stripe.net.RequestOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import com.example.apibackend.enrollment.Enrollment;
import com.example.apibackend.enrollment.EnrollmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final EnrollmentRepository enrollmentRepository;
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

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
        // Prevent duplicate payment: check if user is already enrolled
        boolean alreadyEnrolled = enrollmentRepository.existsByUserIdAndCourseId(userId, courseId);
        if (alreadyEnrolled) {
            logger.warn("User {} is already enrolled in course {}. Blocking duplicate payment.", userId, courseId);
            throw new IllegalStateException("You are already enrolled in this course.");
        }
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
     * Checkout cart with a single payment and PaymentIntent for all cart items.
     * Assumes all courses in the cart have the same currency.
     */
    @Transactional
    public CartCheckoutResponseDTO checkoutCartWithSinglePayment(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found"));
        List<CartItem> items = cart.getItems();
        if (items.isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }
        // Calculate total amount and currency (assume all courses have same currency for simplicity)
        int totalAmount = 0;
        String currency = "USD";
        List<Long> courseIds = new java.util.ArrayList<>();
        for (CartItem item : items) {
            Course course = courseRepository.findById(item.getCourseId())
                    .orElseThrow(() -> new IllegalArgumentException("Course not found: " + item.getCourseId()));
            totalAmount += course.getPriceCents();
            currency = course.getCurrency() != null ? course.getCurrency() : currency;
            courseIds.add(course.getId());
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        // Create Payment
        Payment payment = new Payment();
        payment.setUser(user);
        payment.setAmountCents(totalAmount);
        payment.setCurrency(currency);
        payment.setStatus(Payment.PaymentStatus.PENDING);
        payment.setGatewayTxnId(null);
        payment = paymentRepository.save(payment);
        // Create Stripe PaymentIntent for total amount
        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount((long) totalAmount)
                    .setCurrency(currency.toLowerCase())
                    .putMetadata("paymentId", payment.getId().toString())
                    .putMetadata("userId", userId.toString())
                    .setDescription("Cart purchase: " + courseIds.size() + " courses")
                    .build();
            RequestOptions requestOptions = RequestOptions.builder()
                    .setIdempotencyKey("cart-payment-" + payment.getId())
                    .build();
            PaymentIntent intent = PaymentIntent.create(params, requestOptions);
            payment.setGatewayTxnId(intent.getId());
            paymentRepository.save(payment);
            return new CartCheckoutResponseDTO(payment.getId(), totalAmount, currency, payment.getStatus().name(), intent.getClientSecret(), courseIds);
        } catch (StripeException e) {
            throw new RuntimeException("Stripe PaymentIntent creation failed", e);
        }
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


    /**
     * Refunds a successful payment and revokes enrollment if present.
     * Side effects: Updates payment status, records refund timestamp, updates enrollment status, logs audit event.
     * Refunds are admin-only to prevent abuse and ensure proper audit trail.
     */
    @Transactional
    public void refundPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));
        if (payment.getStatus() != Payment.PaymentStatus.SUCCESS) {
            throw new IllegalStateException("Only successful payments can be refunded");
        }
        // Update payment status and refund timestamp
        payment.setStatus(Payment.PaymentStatus.REFUNDED);
        payment.setRefundedAt(java.time.Instant.now());
        paymentRepository.save(payment);
        // Find enrollment for user and course
        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(
                payment.getUser().getId(), payment.getCourse().getId()
        ).orElse(null);
        if (enrollment != null && enrollment.getStatus() == Enrollment.EnrollmentStatus.ACTIVE) {
            // Set enrollment status to CANCELED and record revoked_at
            enrollment.setStatus(Enrollment.EnrollmentStatus.CANCELED);
            enrollment.setRevokedAt(java.time.Instant.now());
            enrollmentRepository.save(enrollment);
        }
        // TODO: Integrate with payment gateway for actual refund if required
        // Log audit event for refund
        logger.info("Admin refunded payment {} for user {} and course {}", paymentId, payment.getUser().getId(), payment.getCourse().getId());
    }
}
