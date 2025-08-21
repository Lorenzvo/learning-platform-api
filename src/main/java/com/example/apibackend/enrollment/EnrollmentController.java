package com.example.apibackend.enrollment;

import com.example.apibackend.user.User;
import com.example.apibackend.user.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Production EnrollmentController: All user-specific actions derive userId from JWT principal.
 * Never trust client-supplied user IDs! This prevents users from accessing or modifying other users' data.
 * This matches stateless JWT auth: the server only trusts the token, not the request body.
 */
@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {
    private final EnrollmentRepository enrollmentRepo;
    private final UserRepository userRepo;

    public EnrollmentController(EnrollmentRepository enrollmentRepo, UserRepository userRepo) {
        this.enrollmentRepo = enrollmentRepo;
        this.userRepo = userRepo;
    }

    /**
     * GET /api/enrollments/me
     * Returns the current user's enrollments as paginated DTOs.
     * User ID is derived from JWT principal, not from request body or params.
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyEnrollments(
            Authentication authentication,
            @org.springframework.data.web.PageableDefault(size = 6) org.springframework.data.domain.Pageable pageable
    ) {
        User principal = (User) authentication.getPrincipal();
        Long userId = principal.getId();
        org.springframework.data.domain.Page<Enrollment> enrollmentsPage = enrollmentRepo.findByUserIdAndUser_DeletedAtIsNull(userId, pageable);
        java.util.List<EnrollmentDto> dtos = enrollmentsPage.getContent().stream().map(EnrollmentDto::new).collect(java.util.stream.Collectors.toList());
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("content", dtos);
        response.put("page", enrollmentsPage.getNumber());
        response.put("size", enrollmentsPage.getSize());
        response.put("totalPages", enrollmentsPage.getTotalPages());
        response.put("totalElements", enrollmentsPage.getTotalElements());
        return ResponseEntity.ok(response);
    }

    /**
     * DTO for enrollment response
     */
    public static class EnrollmentDto {
        public final Long id;
        public final Long courseId;
        public final String slug;
        public final String status;
        public final String courseTitle;      // Add this
        public final String shortDesc;
        public final String thumbnailUrl;
        public EnrollmentDto(Enrollment e) {
            this.id = e.getId();
            this.courseId = e.getCourse().getId();
            this.slug = e.getCourse().getSlug();
            this.courseTitle = e.getCourse().getTitle(); // Add this
            this.shortDesc = e.getCourse().getShortDescription();
            this.status = e.getStatus().toString();
            this.thumbnailUrl = e.getCourse().getThumbnailUrl();
        }
    }
}
