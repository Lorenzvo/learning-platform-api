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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.validation.Valid;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Optional;

@RestController
@RequestMapping("/api/webhooks/payment")
@RequiredArgsConstructor
@Validated // Ensures request DTO validation
public class PaymentWebhookController {
    private final PaymentRepository paymentRepo;
    private final EnrollmentRepository enrollmentRepo;
    private final EmailService emailService;
    private final PaymentItemRepository paymentItemRepository;
    private static final Logger log = LoggerFactory.getLogger(PaymentWebhookController.class);

    // Shared secret for signature verification (stub for now)
    @Value("${webhook.shared-secret:stub-secret}")
    private String sharedSecret;

    @Value("${stripe.webhookSecret}")
    private String stripeWebhookSecret;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * POST /api/webhooks/payment
     * Accepts payment gateway webhook payload and updates payment/enrollment atomically.
     *
     * Strict HMAC verification of X-Signature header using sharedSecret from config.
     * Rejects missing/mismatched signatures with 401 Unauthorized.
     * Clock skew and replay attacks are mitigated by idempotency (payment status check).
     *
     * Frontend: error responses use the global error shape documented in ApiExceptionHandler.
     */
    @PostMapping
    @Transactional
    public ResponseEntity<?> handlePaymentWebhook(
            @Valid @RequestBody PaymentWebhookPayload payload,
            @RequestHeader(value = "X-Signature", required = false) String signature
    ) {
        log.info("Webhook attempt: paymentId={}, gatewayTxnId={}, status={}, signature={}",
                payload.paymentId, payload.gatewayTxnId, payload.status, signature);
        // Strict HMAC verification
        if (signature == null || !verifySignature(payload, signature)) {
            log.warn("Rejected webhook: missing/invalid signature");
            // 401 error shape handled by ApiExceptionHandler
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Invalid signature");
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
            Long userId = payment.getUser().getId();
            // Bulk payment: enroll user in all courses from PaymentItems
            var paymentItems = paymentItemRepository.findAllByPaymentId(payment.getId());
            if (!paymentItems.isEmpty()) {
                for (var item : paymentItems) {
                    Long courseId = item.getCourse().getId();
                    if (!enrollmentRepo.existsByUserIdAndCourseId(userId, courseId)) {
                        Enrollment enrollment = new Enrollment();
                        enrollment.setUser(payment.getUser());
                        enrollment.setCourse(item.getCourse());
                        enrollment.setStatus(Enrollment.EnrollmentStatus.ACTIVE);
                        enrollmentRepo.save(enrollment);
                        emailService.sendEnrollmentConfirmation(payment.getUser(), item.getCourse());
                    }
                    emailService.sendPaymentReceipt(payment.getUser(), item.getCourse(), payment);
                }
            } else if (payment.getCourse() != null) {
                // Single course payment fallback
                Long courseId = payment.getCourse().getId();
                if (!enrollmentRepo.existsByUserIdAndCourseId(userId, courseId)) {
                    Enrollment enrollment = new Enrollment();
                    enrollment.setUser(payment.getUser());
                    enrollment.setCourse(payment.getCourse());
                    enrollment.setStatus(Enrollment.EnrollmentStatus.ACTIVE);
                    enrollmentRepo.save(enrollment);
                    emailService.sendEnrollmentConfirmation(payment.getUser(), payment.getCourse());
                }
                emailService.sendPaymentReceipt(payment.getUser(), payment.getCourse(), payment);
            }
            log.info("Payment marked SUCCESS and enrollment(s) created if needed");
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
     * Stripe webhook handler for payment_intent events.
     * Verifies Stripe-Signature, parses event, and updates payment/enrollment atomically.
     * Handles payment_intent.succeeded and payment_intent.payment_failed.
     * Idempotent: replaying events is safe.
     * Logs all attempts for audit.
     */
    @PostMapping("/stripe")
    @Transactional
    public ResponseEntity<Void> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String sigHeader
    ) {
        log.info("Stripe webhook received: sigHeader={}, payload={}", sigHeader, payload);
        if (sigHeader == null) {
            log.warn("Missing Stripe-Signature header");
            return ResponseEntity.status(403).build();
        }
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, stripeWebhookSecret);
        } catch (Exception e) {
            log.warn("Stripe webhook signature verification failed: {}", e.getMessage());
            return ResponseEntity.status(403).build();
        }
        log.info("Stripe event type: {} id: {}", event.getType(), event.getId());
        if ("payment_intent.succeeded".equals(event.getType()) || "payment_intent.payment_failed".equals(event.getType())) {
            JsonNode dataObject;
            try {
                Object stripeObject = event.getData().getObject();
                if (stripeObject instanceof String) {
                    dataObject = objectMapper.readTree((String) stripeObject);
                } else {
                    dataObject = objectMapper.valueToTree(stripeObject);
                }
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                log.error("Failed to parse Stripe event data object: {}", e.getMessage());
                return ResponseEntity.ok().build();
            }
            String paymentIntentId = dataObject.has("id") ? dataObject.get("id").asText() : null;
            String status = "payment_intent.succeeded".equals(event.getType()) ? "SUCCESS" : "FAILED";
            Long paymentId = null;
            if (dataObject.has("metadata") && dataObject.get("metadata").has("paymentId")) {
                try {
                    paymentId = Long.valueOf(dataObject.get("metadata").get("paymentId").asText());
                } catch (Exception ex) {
                    log.warn("Could not parse paymentId from Stripe metadata: {}", ex.getMessage());
                }
            }
            Payment payment = null;
            if (paymentId != null) {
                payment = paymentRepo.findById(paymentId).orElse(null);
            } else if (paymentIntentId != null) {
                payment = paymentRepo.findByGatewayTxnId(paymentIntentId).orElse(null);
            }
            if (payment == null) {
                log.warn("No matching Payment found for Stripe PaymentIntent id {}", paymentIntentId);
                return ResponseEntity.ok().build();
            }
            // Idempotency: if already processed, return 200
            if (payment.getStatus() == Payment.PaymentStatus.SUCCESS && "SUCCESS".equals(status)) {
                log.info("Payment already marked SUCCESS, idempotent webhook");
                return ResponseEntity.ok().build();
            }
            if (payment.getStatus() == Payment.PaymentStatus.FAILED && "FAILED".equals(status)) {
                log.info("Payment already marked FAILED, idempotent webhook");
                return ResponseEntity.ok().build();
            }
            if ("SUCCESS".equals(status) && payment.getStatus() == Payment.PaymentStatus.PENDING) {
                payment.setStatus(Payment.PaymentStatus.SUCCESS);
                payment.setGatewayTxnId(paymentIntentId);
                paymentRepo.save(payment);
                Long userId = payment.getUser().getId();
                var paymentItems = paymentItemRepository.findAllByPaymentId(payment.getId());
                if (!paymentItems.isEmpty()) {
                    for (var item : paymentItems) {
                        Long courseId = item.getCourse().getId();
                        boolean alreadyEnrolled = enrollmentRepo.existsByUserIdAndCourseId(userId, courseId);
                        if (!alreadyEnrolled) {
                            Enrollment enrollment = new Enrollment();
                            enrollment.setUser(payment.getUser());
                            enrollment.setCourse(item.getCourse());
                            enrollment.setStatus(Enrollment.EnrollmentStatus.ACTIVE);
                            enrollmentRepo.save(enrollment);
                            emailService.sendEnrollmentConfirmation(payment.getUser(), item.getCourse());
                        }
                        emailService.sendPaymentReceipt(payment.getUser(), item.getCourse(), payment);
                    }
                } else if (payment.getCourse() != null) {
                    Long courseId = payment.getCourse().getId();
                    boolean alreadyEnrolled = enrollmentRepo.existsByUserIdAndCourseId(userId, courseId);
                    if (!alreadyEnrolled) {
                        Enrollment enrollment = new Enrollment();
                        enrollment.setUser(payment.getUser());
                        enrollment.setCourse(payment.getCourse());
                        enrollment.setStatus(Enrollment.EnrollmentStatus.ACTIVE);
                        enrollmentRepo.save(enrollment);
                        emailService.sendEnrollmentConfirmation(payment.getUser(), payment.getCourse());
                    }
                    emailService.sendPaymentReceipt(payment.getUser(), payment.getCourse(), payment);
                }
                log.info("Payment marked SUCCESS and enrollment(s) created if needed");
            } else if ("FAILED".equals(status)) {
                payment.setStatus(Payment.PaymentStatus.FAILED);
                paymentRepo.save(payment);
                log.info("Payment {} marked as FAILED via Stripe webhook", payment.getId());
            }
            // Always return 200 for processed webhooks
            return ResponseEntity.ok().build();
        }
        // Log and ignore other event types (test events, etc.)
        log.info("Unhandled Stripe event type: {}", event.getType());
        return ResponseEntity.ok().build();
    }

    /**
     * Verifies HMAC-SHA256 signature of payload using sharedSecret.
     * Returns true if valid, false otherwise.
     */
    private boolean verifySignature(PaymentWebhookPayload payload, String signature) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(sharedSecret.getBytes(), "HmacSHA256"));
            String payloadJson = objectMapper.writeValueAsString(payload);
            byte[] computed = mac.doFinal(payloadJson.getBytes());
            String expected = Base64.getEncoder().encodeToString(computed);
            return expected.equals(signature);
        } catch (Exception e) {
            log.error("Signature verification error", e);
            return false;
        }
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
