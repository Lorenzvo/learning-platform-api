package com.example.apibackend.review;

import com.example.apibackend.enrollment.EnrollmentRepository;
import com.example.apibackend.enrollment.Enrollment;
import com.example.apibackend.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.Optional;

@RestController
@RequestMapping("/api/courses/{courseId}/reviews")
public class ReviewController {
    private final ReviewRepository reviewRepo;
    private final EnrollmentRepository enrollmentRepo;

    public ReviewController(ReviewRepository reviewRepo, EnrollmentRepository enrollmentRepo) {
        this.reviewRepo = reviewRepo;
        this.enrollmentRepo = enrollmentRepo;
    }

    @GetMapping
    public Page<Review> getReviews(@PathVariable Long courseId, @PageableDefault(size = 10) Pageable pageable) {
        return reviewRepo.findByCourseId(courseId, pageable);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> postReview(
            @PathVariable Long courseId,
            @AuthenticationPrincipal User user,
            @RequestBody ReviewRequest req
    ) {
        // Only enrolled users can post
        Optional<Enrollment> enrollmentOpt = enrollmentRepo.findByUserIdAndCourseId(user.getId(), courseId);
        if (enrollmentOpt.isEmpty() || enrollmentOpt.get().getStatus() != Enrollment.EnrollmentStatus.ACTIVE) {
            return ResponseEntity.status(403).body("Must be enrolled to review");
        }
        // Anti-spam: min length and cooldown (5 min)
        if (req.comment == null || req.comment.trim().length() < 10) {
            return ResponseEntity.badRequest().body("Comment too short");
        }
        Instant fiveMinutesAgo = Instant.now().minusSeconds(300);
        boolean recent = reviewRepo.findByCourseId(courseId).stream()
            .anyMatch(r -> r.getUser().getId().equals(user.getId()) && r.getCreatedAt().isAfter(fiveMinutesAgo));
        if (recent) {
            return ResponseEntity.status(429).body("Please wait before posting another review");
        }
        // Save review
        Review review = new Review();
        review.setUser(user);
        // Fix: setCourse expects a Course, not a courseId
        com.example.apibackend.course.Course course = enrollmentOpt.get().getCourse();
        review.setCourse(course);
        review.setRating(req.rating);
        review.setComment(req.comment);
        review.setCreatedAt(Instant.now());
        reviewRepo.save(review);
        return ResponseEntity.status(201).body(review);
    }

    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteReview(@PathVariable Long courseId, @PathVariable Long reviewId) {
        Optional<Review> reviewOpt = reviewRepo.findById(reviewId);
        if (reviewOpt.isEmpty() || !reviewOpt.get().getCourse().getId().equals(courseId)) {
            return ResponseEntity.notFound().build();
        }
        reviewRepo.deleteById(reviewId);
        return ResponseEntity.noContent().build();
    }

    public static class ReviewRequest {
        public int rating;
        public String comment;
    }
}
