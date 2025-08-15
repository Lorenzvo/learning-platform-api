package com.example.apibackend.enrollment;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

