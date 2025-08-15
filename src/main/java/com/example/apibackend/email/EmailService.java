package com.example.apibackend.email;

import com.example.apibackend.user.User;
import com.example.apibackend.course.Course;
import com.example.apibackend.payment.Payment;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    // Config flag to enable/disable emails in dev
    @Value("${email.enabled:true}")
    private boolean emailEnabled;

    /**
     * Sends a payment receipt email to the user (stub: logs for now).
     * In production, integrate SES/SendGrid here.
     */
    public void sendPaymentReceipt(User user, Course course, Payment payment) {
        if (!emailEnabled) {
            log.info("[EmailService] Email disabled by config. Skipping payment receipt for user {}", user.getEmail());
            return;
        }
        // Stub: log the email event. Replace with SES/SendGrid integration in production.
        log.info("[EmailService] Sent payment receipt to {} for course {} (paymentId={})", user.getEmail(), course.getTitle(), payment.getId());
    }

    /**
     * Sends an enrollment confirmation email to the user (stub: logs for now).
     * In production, integrate SES/SendGrid here.
     */
    public void sendEnrollmentConfirmation(User user, Course course) {
        if (!emailEnabled) {
            log.info("[EmailService] Email disabled by config. Skipping enrollment confirmation for user {}", user.getEmail());
            return;
        }
        // Stub: log the email event. Replace with SES/SendGrid integration in production.
        log.info("[EmailService] Sent enrollment confirmation to {} for course {}", user.getEmail(), course.getTitle());
    }
}

