package com.example.apibackend.course;

import com.example.apibackend.enrollment.EnrollmentRepository;
import com.example.apibackend.payment.Payment;
import com.example.apibackend.payment.PaymentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Stub admin endpoint for security testing.
 */
@RestController
@RequestMapping("/api/admin/courses")
@PreAuthorize("hasRole('ADMIN')") // Enforce ROLE_ADMIN for all endpoints in this controller
public class AdminCourseController {
    private final CourseRepository courseRepo;
    private final EnrollmentRepository enrollmentRepo;
    private final PaymentRepository paymentRepo;

    public AdminCourseController(CourseRepository courseRepo, EnrollmentRepository enrollmentRepo, PaymentRepository paymentRepo) {
        this.courseRepo = courseRepo;
        this.enrollmentRepo = enrollmentRepo;
        this.paymentRepo = paymentRepo;
    }

    @GetMapping
    public ResponseEntity<?> getAdminCourses() {
        // Stub: just return 200 OK for security test
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public ResponseEntity<?> createCourse(@Validated @RequestBody CreateCourseRequest req) {
        // Validation groups can be used for different create/update scenarios (not shown here)
        // Common failure: duplicate slug (should be unique)
        if (courseRepo.existsBySlug(req.slug)) {
            return ResponseEntity.status(409).body("Course slug already exists");
        }
        Course course = new Course();
        course.setTitle(req.title);
        course.setSlug(req.slug);
        course.setPriceCents(req.price);
        course.setCurrency(req.currency);
        course.setLevel(req.level);
        course.setShortDescription(req.shortDesc);
        course.setDescription(req.longDesc);
        course.setThumbnailUrl(req.thumbnailUrl);
        course.setIsActive(req.published);
        courseRepo.save(course);
        // Return CourseDetailDto (assume constructor from Course)
        return ResponseEntity.status(201).body(new CourseDetailDto(course));
    }

    @GetMapping("/metrics/summary")
    public ResponseEntity<MetricsSummary> getMetricsSummary() {
        // These queries are lightweight for small/medium datasets, but may need caching for large scale.
        long totalCourses = courseRepo.count();
        long totalEnrollments = enrollmentRepo.count();
        // Sum revenue from successful payments; if payments table not populated, return 0
        Double revenueUsd = paymentRepo.sumAmountByStatus(Payment.PaymentStatus.SUCCESS);
        if (revenueUsd == null) revenueUsd = 0.0;
        // Convert cents to dollars for API response
        revenueUsd = revenueUsd / 100.0;
        MetricsSummary summary = new MetricsSummary(totalCourses, totalEnrollments, revenueUsd);
        return ResponseEntity.ok(summary);
    }

    /**
     * DTO for course creation. Validation groups can be added for more granular control.
     */
    public static class CreateCourseRequest {
        @NotBlank
        @Size(max = 255)
        public String title;
        @NotBlank
        @Size(max = 255)
        public String slug;
        @NotNull
        public Integer price;
        @NotBlank
        @Size(max = 3)
        public String currency;
        @NotBlank
        @Size(max = 50)
        public String level;
        @NotBlank
        @Size(max = 280)
        public String shortDesc;
        @NotBlank
        public String longDesc;
        @NotBlank
        @Size(max = 255)
        public String thumbnailUrl;
        @NotNull
        public Boolean published;
    }

    /**
         * Simple DTO for metrics summary response
         */
        public record MetricsSummary(long totalCourses, long totalEnrollments, double revenueUsd) {
    }
}
