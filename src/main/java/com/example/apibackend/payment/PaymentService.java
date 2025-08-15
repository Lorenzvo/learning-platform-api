package com.example.apibackend.payment;

import com.example.apibackend.course.Course;
import com.example.apibackend.course.CourseRepository;
import com.example.apibackend.user.User;
import com.example.apibackend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

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

    // Converts Payment entity to CheckoutResponseDTO
    private CheckoutResponseDTO toCheckoutDTO(Payment payment) {
        // In real gateway integration, clientSecret would come from Stripe/Razorpay
        String clientSecret = "cs_test_" + (payment.getId() != null ? payment.getId() : UUID.randomUUID());
        return new CheckoutResponseDTO(payment.getId(), clientSecret, payment.getAmountCents(), payment.getCurrency(), payment.getStatus().name());
    }
}
