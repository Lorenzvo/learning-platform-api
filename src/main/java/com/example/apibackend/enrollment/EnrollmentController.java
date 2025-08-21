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
     * Returns the current user's enrollments as DTOs.
     * User ID is derived from JWT principal, not from request body or params.
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EnrollmentDto>> getMyEnrollments(Authentication authentication) {
        // Extract user ID from JWT principal
        User principal = (User) authentication.getPrincipal();
        Long userId = principal.getId();
        List<Enrollment> enrollments = enrollmentRepo.findByUserIdAndUser_DeletedAtIsNull(userId);
        List<EnrollmentDto> dtos = enrollments.stream().map(EnrollmentDto::new).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
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
