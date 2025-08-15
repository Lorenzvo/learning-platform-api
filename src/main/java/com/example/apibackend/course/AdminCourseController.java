package com.example.apibackend.course;

import com.example.apibackend.enrollment.EnrollmentRepository;
import com.example.apibackend.payment.Payment;
import com.example.apibackend.payment.PaymentRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCourse(@PathVariable Long id, @Validated @RequestBody UpdateCourseRequest req) {
        var courseOpt = courseRepo.findById(id);
        if (courseOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var course = courseOpt.get();
        // Validate slug uniqueness (409 Conflict if duplicate)
        if (req.getSlug() != null && !req.getSlug().equals(course.getSlug())) {
            boolean slugExists = courseRepo.existsBySlug(req.getSlug());
            if (slugExists) {
                // Slug must be unique for SEO and routing
                return ResponseEntity.status(409).body("Slug already exists");
            }
            course.setSlug(req.getSlug());
        }
        // Update editable fields
        if (req.getTitle() != null) course.setTitle(req.getTitle());
        if (req.getShortDescription() != null) course.setShortDescription(req.getShortDescription());
        if (req.getThumbnailUrl() != null) course.setThumbnailUrl(req.getThumbnailUrl());
        if (req.getPriceCents() != null) course.setPriceCents(req.getPriceCents());
        if (req.getCurrency() != null) course.setCurrency(req.getCurrency());
        // ...add more editable fields as needed...
        courseRepo.save(course);
        // Return updated CourseDetailDto
        return ResponseEntity.ok(new CourseDetailDto(course));
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<?> publishCourse(@PathVariable Long id) {
        var courseOpt = courseRepo.findById(id);
        if (courseOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var course = courseOpt.get();
        course.setIsActive(true);
        courseRepo.save(course);
        // Publish is explicit to avoid accidental exposure of incomplete courses
        return ResponseEntity.ok(new CourseDetailDto(course));
    }

    @PostMapping("/{id}/unpublish")
    public ResponseEntity<?> unpublishCourse(@PathVariable Long id) {
        var courseOpt = courseRepo.findById(id);
        if (courseOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var course = courseOpt.get();
        course.setIsActive(false);
        courseRepo.save(course);
        return ResponseEntity.ok(new CourseDetailDto(course));
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

    /**
     * DTO for course update. All fields optional for PATCH-like semantics.
     */
    @Getter
    @Setter
    public static class UpdateCourseRequest {
        private String title;
        private String slug;
        private String shortDescription;
        private String thumbnailUrl;
        private Integer priceCents;
        private String currency;
        // ...add more editable fields as needed...

    }
}
