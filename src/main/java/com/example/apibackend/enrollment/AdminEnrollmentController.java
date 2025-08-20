package com.example.apibackend.enrollment;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.format.annotation.DateTimeFormat;
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
    public ResponseEntity<String> exportEnrollmentsCsv(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        StringBuilder csv = new StringBuilder();
        csv.append("enrollmentId,userEmail,courseSlug,enrolledAt,status\n");
        enrollmentRepo.findAll().stream()
            .filter(e -> e.getCreatedAt() != null &&
                !e.getCreatedAt().isBefore(from.atStartOfDay().toInstant(ZoneOffset.UTC)) &&
                e.getCreatedAt().isBefore(to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)))
            .forEach(e -> {
                String userEmail = e.getUser() != null ? e.getUser().getEmail() : "";
                String courseSlug = e.getCourse() != null ? e.getCourse().getSlug() : "";
                String enrolledAt = e.getCreatedAt() != null ? e.getCreatedAt().toString() : "";
                String status = e.getStatus() != null ? e.getStatus().name() : "";
                csv.append(String.format("%d,%s,%s,%s,%s\n",
                    e.getId(),
                    userEmail,
                    courseSlug,
                    enrolledAt,
                    status));
            });
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=enrollments.csv")
            .header("Content-Type", "text/csv")
            .body(csv.toString());
    }

    /**
     * DTO for enrollment response
     * Includes userId for admin visibility.
     */
    public static class EnrollmentDto {
        private Long id;
        private String date; // or LocalDateTime
        private String userEmail;
        private String courseTitle;
        private String status;

        public EnrollmentDto(Enrollment e) {
            this.id = e.getId();
            this.date = e.getCreatedAt().toString(); // assuming Enrollment.getCreatedAt() returns a Date or LocalDateTime
            this.userEmail = e.getUser().getEmail(); // assuming Enrollment.getUser().getEmail()
            this.courseTitle = e.getCourse().getTitle(); // assuming Enrollment.getCourse().getTitle()
            this.status = e.getStatus().toString();
        }

        public Long getId() { return id; }
        public String getDate() { return date; }
        public String getUserEmail() { return userEmail; }
        public String getCourseTitle() { return courseTitle; }
        public String getStatus() { return status; }
    }
}
