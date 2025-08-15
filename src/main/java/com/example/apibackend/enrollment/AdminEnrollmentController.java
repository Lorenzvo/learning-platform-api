package com.example.apibackend.enrollment;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AdminEnrollmentController: Handles admin-only enrollment endpoints.
 *
 * All endpoints here are protected by @PreAuthorize to ensure only admins can access them.
 * This keeps admin logic separate from user logic and makes security easier to manage.
 *
 * Endpoint: GET /api/admin/enrollments
 * Returns all enrollments in the system as DTOs.
 */
@RestController
@RequestMapping("/api/admin/enrollments")
public class AdminEnrollmentController {
    private final EnrollmentRepository enrollmentRepo;

    public AdminEnrollmentController(EnrollmentRepository enrollmentRepo) {
        this.enrollmentRepo = enrollmentRepo;
    }

    /**
     * GET /api/admin/enrollments
     * Only accessible to users with ROLE_ADMIN.
     * Returns all enrollments as DTOs.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EnrollmentDto>> getAllEnrollments() {
        List<Enrollment> enrollments = enrollmentRepo.findAll();
        List<EnrollmentDto> dtos = enrollments.stream().map(EnrollmentDto::new).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * GET /api/admin/reports/enrollments.csv?from=YYYY-MM-DD&to=YYYY-MM-DD
     * Streams CSV for BI/reporting. Streaming avoids memory pressure for large datasets.
     * BI tooling (Excel, Tableau, etc.) can ingest these files directly.
     */
    @GetMapping("/reports/enrollments.csv")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StreamingResponseBody> exportEnrollmentsCsv(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        // Convert LocalDate to Instant for filtering
        Date fromDate = Date.from(from.atStartOfDay().toInstant(ZoneOffset.UTC));
        Date toDate = Date.from(to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC));
        // StreamingResponseBody writes directly to output stream
        StreamingResponseBody stream = out -> {
            // CSV header
            out.write("enrollmentId,userEmail,courseSlug,enrolledAt,status\n".getBytes());
            // Stream enrollments in date range
            enrollmentRepo.findAll().stream()
                .filter(e -> e.getCreatedAt() != null &&
                    !e.getCreatedAt().isBefore(from.atStartOfDay().toInstant(ZoneOffset.UTC)) &&
                    e.getCreatedAt().isBefore(to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)))
                .forEach(e -> {
                    try {
                        String line = String.format("%d,%s,%s,%s,%s\n",
                                e.getId(),
                                e.getUser().getEmail(),
                                e.getCourse().getSlug(),
                                e.getCreatedAt(),
                                e.getStatus().name());
                        out.write(line.getBytes());
                    } catch (Exception ex) { /* log or ignore */ }
                });
        };
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=enrollments.csv")
                .header("Content-Type", "text/csv")
                .body(stream);
    }

    /**
     * DTO for enrollment response
     * Includes userId for admin visibility.
     */
    public static class EnrollmentDto {
        public final Long id;
        public final Long userId;
        public final Long courseId;
        public final String status;
        public EnrollmentDto(Enrollment e) {
            this.id = e.getId();
            this.userId = e.getUser().getId();
            this.courseId = e.getCourse().getId();
            this.status = e.getStatus().toString();
        }
    }
}
