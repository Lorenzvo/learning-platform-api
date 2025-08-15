package com.example.apibackend.payment;

import com.example.apibackend.enrollment.Enrollment;
import com.example.apibackend.enrollment.EnrollmentRepository;
import com.example.apibackend.user.User;
import com.example.apibackend.course.Course;
import com.example.apibackend.email.EmailService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/webhooks/payment")
@RequiredArgsConstructor

public class PaymentWebhookController {
    private final PaymentRepository paymentRepo;
    private final EnrollmentRepository enrollmentRepo;
    private final EmailService emailService;
    private static final Logger log = LoggerFactory.getLogger(PaymentWebhookController.class);

    // Shared secret for signature verification (stub for now)
    @Value("${webhook.shared-secret:stub-secret}")
    private String sharedSecret;

    /**
     * POST /api/webhooks/payment
     * Accepts payment gateway webhook payload and updates payment/enrollment atomically.
     * Signature verification is stubbed for now.
     */

    @PostMapping
    @Transactional
    public ResponseEntity<?> handlePaymentWebhook(
            @RequestBody PaymentWebhookPayload payload,
            @RequestHeader(value = "X-Signature", required = false) String signature
    ) {
        // Log all attempts for audit
        log.info("Webhook attempt: paymentId={}, gatewayTxnId={}, status={}, signature={}",
                payload.paymentId, payload.gatewayTxnId, payload.status, signature);

        // Signature verification (stub): always reject if missing/invalid
        if (signature == null || !verifySignature(payload, signature)) {
            log.warn("Rejected webhook: missing/invalid signature");
            return ResponseEntity.status(401).body("Invalid signature");
        }

        Optional<Payment> optPayment = paymentRepo.findById(payload.paymentId);
        if (optPayment.isEmpty()) {
            log.warn("Payment not found: {}", payload.paymentId);
            return ResponseEntity.status(404).body("Payment not found");
        }
        Payment payment = optPayment.get();

        // Idempotency: if already processed, return 200 with no changes
        if (payment.getStatus() == Payment.PaymentStatus.SUCCESS && "SUCCESS".equalsIgnoreCase(payload.status)) {
            log.info("Payment already marked SUCCESS, idempotent webhook");
            return ResponseEntity.ok("Already processed");
        }
        if (payment.getStatus() == Payment.PaymentStatus.FAILED && "FAILED".equalsIgnoreCase(payload.status)) {
            log.info("Payment already marked FAILED, idempotent webhook");
            return ResponseEntity.ok("Already processed");
        }

        // Handle status transitions
        if ("SUCCESS".equalsIgnoreCase(payload.status) && payment.getStatus() == Payment.PaymentStatus.PENDING) {
            payment.setStatus(Payment.PaymentStatus.SUCCESS);
            payment.setGatewayTxnId(payload.gatewayTxnId);
            paymentRepo.save(payment);
            User user = payment.getUser();
            Course course = payment.getCourse();
            boolean alreadyEnrolled = enrollmentRepo.existsByUserIdAndCourseId(user.getId(), course.getId());
            if (!alreadyEnrolled) {
                Enrollment enrollment = new Enrollment();
                enrollment.setUser(user);
                enrollment.setCourse(course);
                enrollment.setStatus(Enrollment.EnrollmentStatus.ACTIVE);
                enrollmentRepo.save(enrollment);
                // Send enrollment confirmation email (stub)
                emailService.sendEnrollmentConfirmation(user, course);
            }
            // Always send payment receipt email (stub)
            emailService.sendPaymentReceipt(user, course, payment);
            log.info("Payment marked SUCCESS and enrollment created if needed");
            return ResponseEntity.ok("Payment processed and enrollment updated");
        } else if ("FAILED".equalsIgnoreCase(payload.status)) {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            paymentRepo.save(payment);
            log.info("Payment marked FAILED");
            return ResponseEntity.ok("Payment marked FAILED");
        }

        // Unhandled status or already processed
        log.warn("Unhandled or duplicate webhook status: {} for payment {}", payload.status, payment.getId());
        return ResponseEntity.ok("No changes");
    }

    /**
     * Stub for signature verification. Replace with HMAC or gateway-specific logic.
     * Always reject if missing/invalid. Never trust client calls for enrollment.
     */
    private boolean verifySignature(PaymentWebhookPayload payload, String signature) {
        // TODO: Implement HMAC or gateway signature verification using sharedSecret
        // For now, accept only if signature equals sharedSecret (stub)
        return sharedSecret.equals(signature);
    }

    /**
     * DTO for webhook payload
     */
    @Data
    public static class PaymentWebhookPayload {
        public Long paymentId;
        public String gatewayTxnId;
        public String status;
    }
}

