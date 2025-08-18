package com.example.apibackend.payment;

import com.example.apibackend.course.Course;
import com.example.apibackend.user.User;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@RestController
@RequestMapping("/api/admin/payments")
public class AdminPaymentController {
    private final PaymentRepository paymentRepo;
    private final PaymentService paymentService;

    public AdminPaymentController(PaymentRepository paymentRepo, PaymentService paymentService) {
        this.paymentRepo = paymentRepo;
        this.paymentService = paymentService;
    }

    /**
     * GET /api/admin/reports/payments.csv?from=YYYY-MM-DD&to=YYYY-MM-DD
     * Streams CSV for BI/reporting. Streaming avoids memory pressure for large datasets.
     * BI tooling (Excel, Tableau, etc.) can ingest these files directly.
     */
    @GetMapping("/reports/payments.csv")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StreamingResponseBody> exportPaymentsCsv(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        StreamingResponseBody stream = out -> {
            // CSV header
            out.write("paymentId,userEmail,courseSlug,amount,currency,status,gatewayTxnId,createdAt\n".getBytes());
            // Stream payments in date range
            paymentRepo.findAll().stream()
                .filter(p -> p.getCreatedAt() != null &&
                    !p.getCreatedAt().isBefore(from.atStartOfDay().toInstant(ZoneOffset.UTC)) &&
                    p.getCreatedAt().isBefore(to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)))
                .forEach(p -> {
                    try {
                        User user = p.getUser();
                        Course course = p.getCourse();
                        String line = String.format("%d,%s,%s,%d,%s,%s,%s,%s\n",
                                p.getId(),
                                user.getEmail(),
                                course.getSlug(),
                                p.getAmountCents(),
                                p.getCurrency(),
                                p.getStatus().name(),
                                p.getGatewayTxnId(),
                                p.getCreatedAt());
                        out.write(line.getBytes());
                    } catch (Exception ex) { /* log or ignore */ }
                });
        };
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=payments.csv")
                .header("Content-Type", "text/csv")
                .body(stream);
    }

    /**
     * POST /api/admin/payments/{paymentId}/refund
     * Admin-only: Refunds a successful payment and revokes enrollment if present.
     * Side effects: Updates payment status, records refund timestamp, updates enrollment status, logs audit event.
     * Refunds are admin-only to prevent abuse and ensure proper audit trail.
     */
    @PostMapping("/{paymentId}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> refundPayment(@PathVariable Long paymentId) {
        // Call service to process refund
        paymentService.refundPayment(paymentId);
        return ResponseEntity.ok().build();
    }
}
