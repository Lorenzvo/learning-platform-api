package com.example.apibackend.payment;

import com.example.apibackend.course.Course;
import com.example.apibackend.user.User;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

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
    // Change to StreamingResponseBody if dataset is large, but til the far future we assume manageable size
    public ResponseEntity<String> exportPaymentsCsv(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        StringBuilder csv = new StringBuilder();
        csv.append("paymentId,userEmail,courseSlug,amount,currency,status,gatewayTxnId,createdAt\n");
        paymentRepo.findAll().stream()
            .filter(p -> p.getCreatedAt() != null &&
                !p.getCreatedAt().isBefore(from.atStartOfDay().toInstant(ZoneOffset.UTC)) &&
                p.getCreatedAt().isBefore(to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)))
            .forEach(p -> {
                String userEmail = p.getUser() != null ? p.getUser().getEmail() : "";
                String courseSlug = p.getCourse() != null ? p.getCourse().getSlug() : "";
                String amount = p.getAmountCents() != null ? p.getAmountCents().toString() : "";
                String currency = p.getCurrency() != null ? p.getCurrency() : "";
                String status = p.getStatus() != null ? p.getStatus().name() : "";
                String gatewayTxnId = p.getGatewayTxnId() != null ? p.getGatewayTxnId() : "";
                String createdAt = p.getCreatedAt() != null ? p.getCreatedAt().toString() : "";
                csv.append(String.format("%d,%s,%s,%s,%s,%s,%s,%s\n",
                    p.getId(),
                    userEmail,
                    courseSlug,
                    amount,
                    currency,
                    status,
                    gatewayTxnId,
                    createdAt));
            });
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=payments.csv")
            .header("Content-Type", "text/csv")
            .body(csv.toString());
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

    /**
     * GET /api/admin/payments?from=yyyy-MM-dd&to=yyyy-MM-dd
     * Admin-only: Returns payments in the requested date range as JSON.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PaymentDto>> getPaymentsInRange(
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        List<PaymentDto> payments = paymentService.getPaymentsInRange(from, to);
        return ResponseEntity.ok(payments);
    }
}
