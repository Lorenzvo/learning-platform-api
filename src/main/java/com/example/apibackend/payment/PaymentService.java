package com.example.apibackend.payment;

import com.example.apibackend.cart.CartRepository;
import com.example.apibackend.cart.Cart;
import com.example.apibackend.cart.CartItem;
import com.example.apibackend.course.Course;
import com.example.apibackend.course.CourseRepository;
import com.example.apibackend.user.User;
import com.example.apibackend.user.UserRepository;
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
        // Check for existing PENDING payment for this user+course (idempotency)
        Optional<Payment> existing = paymentRepository.findTopByUserIdAndCourseIdAndStatusOrderByCreatedAtDesc(
                userId, courseId, Payment.PaymentStatus.PENDING);
        if (existing.isPresent()) {
            Payment payment = existing.get();
            // Return existing payment (idempotency)
            return toCheckoutDTO(payment);
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
        // Generate a server-side receiptId (for audit/tracking)
        payment.setGatewayTxnId(null); // Will be filled by gateway webhook later
        // Placeholder clientSecret (replace with Stripe/Razorpay integration)
        String clientSecret = "cs_test_" + UUID.randomUUID();
        // Save payment row
        payment = paymentRepository.save(payment);

        // NOTE: We write a PENDING row first to track intent and prevent duplicate charges.
        // Actual payment gateway (Stripe/Razorpay) will update gateway_txn_id and status via webhook.
        // For real integration, generate clientSecret from gateway API and store gatewayTxnId.
        // For multi-course/cart, refactor Payment to support multiple courses and update logic here.

        return new CheckoutResponseDTO(payment.getId(), clientSecret, payment.getAmountCents(), payment.getCurrency(), payment.getStatus().name());
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
            if (existing.isPresent()) {
                payment = existing.get();
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
                payment.setGatewayTxnId(null); // Will be filled by gateway webhook
                payment = paymentRepository.save(payment);
            }
            // Generate clientSecret (stub for Stripe/Razorpay)
            String clientSecret = "cs_test_" + (payment.getId() != null ? payment.getId() : java.util.UUID.randomUUID());
            paymentDTOs.add(new CartCheckoutResponseDTO.CartPaymentDTO(
                    payment.getId(), course.getId(), payment.getAmountCents(), payment.getCurrency(), payment.getStatus().name(), clientSecret
            ));
        }
        // NOTE: Multiple payments are acceptable for MVP. In future, use payment_items for single charge.
        return new CartCheckoutResponseDTO(paymentDTOs);
    }

    // Converts Payment entity to CheckoutResponseDTO
    private CheckoutResponseDTO toCheckoutDTO(Payment payment) {
        // In real gateway integration, clientSecret would come from Stripe/Razorpay
        String clientSecret = "cs_test_" + (payment.getId() != null ? payment.getId() : UUID.randomUUID());
        return new CheckoutResponseDTO(payment.getId(), clientSecret, payment.getAmountCents(), payment.getCurrency(), payment.getStatus().name());
    }
}
