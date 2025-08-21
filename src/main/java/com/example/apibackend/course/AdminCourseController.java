package com.example.apibackend.course;

import com.example.apibackend.enrollment.EnrollmentRepository;
import com.example.apibackend.payment.Payment;
import com.example.apibackend.payment.PaymentRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger logger = LoggerFactory.getLogger(AdminCourseController.class);

    public AdminCourseController(CourseRepository courseRepo, EnrollmentRepository enrollmentRepo, PaymentRepository paymentRepo) {
        this.courseRepo = courseRepo;
        this.enrollmentRepo = enrollmentRepo;
        this.paymentRepo = paymentRepo;
    }

    @GetMapping
    public ResponseEntity<?> getAdminCourses(
            @org.springframework.data.web.PageableDefault(size = 6) org.springframework.data.domain.Pageable pageable,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) Boolean published
    ) {
        org.springframework.data.domain.Page<Course> page = courseRepo.search(q, level, published, pageable);
        java.util.List<AdminCourseDto> dtos = page.getContent().stream().map(AdminCourseDto::fromEntity).toList();
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("content", dtos);
        response.put("page", page.getNumber());
        response.put("size", page.getSize());
        response.put("totalPages", page.getTotalPages());
        response.put("totalElements", page.getTotalElements());
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<?> createCourse(@Validated @RequestBody CreateCourseRequest req) {
        logger.info("Admin attempting to create course: title={}, slug={}", req.title, req.slug);
        // Validation groups can be used for different create/update scenarios (not shown here)
        // Common failure: duplicate slug (should be unique)
        if (courseRepo.existsBySlug(req.slug)) {
            logger.warn("Course creation failed: slug '{}' already exists", req.slug);
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
        logger.info("Course created successfully: id={}, slug={}", course.getId(), course.getSlug());
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
        logger.info("Admin attempting to update course: id={}, slug={}", id, req.getSlug());
        var courseOpt = courseRepo.findById(id);
        if (courseOpt.isEmpty()) {
            logger.warn("Course update failed: id '{}' not found", id);
            return ResponseEntity.notFound().build();
        }
        var course = courseOpt.get();
        // Update editable fields
        if (req.getTitle() != null) course.setTitle(req.getTitle());
        if (req.getSlug() != null && !req.getSlug().equals(course.getSlug())) {
            boolean slugExists = courseRepo.existsBySlug(req.getSlug());
            if (slugExists) {
                return ResponseEntity.status(409).body("Slug already exists");
            }
            course.setSlug(req.getSlug());
        }
        if (req.getPriceCents() != null) course.setPriceCents(req.getPriceCents());
        if (req.getCurrency() != null) course.setCurrency(req.getCurrency());
        if (req.getLevel() != null) course.setLevel(req.getLevel());
        if (req.getShortDescription() != null) course.setShortDescription(req.getShortDescription());
        if (req.getDescription() != null) course.setDescription(req.getDescription());
        if (req.getThumbnailUrl() != null) course.setThumbnailUrl(req.getThumbnailUrl());
        if (req.getPublished() != null) course.setIsActive(req.getPublished());
        // ...add more editable fields as needed...
        courseRepo.save(course);
        logger.info("Course updated successfully: id={}, slug={}", course.getId(), course.getSlug());
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

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCourse(@PathVariable Long id) {
        var courseOpt = courseRepo.findById(id);
        if (courseOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        courseRepo.deleteById(id);
        return ResponseEntity.noContent().build();
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
        private String level;
        private String description;
        private Boolean published;
        // ...add more editable fields as needed...

    }

    public static class AdminCourseDto {
        public final Long id;
        public final String title;
        public final String slug;
        public final String shortDescription;
        public final String description;
        public final Integer priceCents;
        public final String currency;
        public final String level;
        public final Boolean isActive;
        public final String thumbnailUrl;
        public AdminCourseDto(Long id, String title, String slug, String shortDescription, String description, Integer priceCents, String currency, String level, Boolean isActive, String thumbnailUrl) {
            this.id = id;
            this.title = title;
            this.slug = slug;
            this.shortDescription = shortDescription;
            this.description = description;
            this.priceCents = priceCents;
            this.currency = currency;
            this.level = level;
            this.isActive = isActive;
            this.thumbnailUrl = thumbnailUrl;
        }
        public static AdminCourseDto fromEntity(Course c) {
            return new AdminCourseDto(
                c.getId(),
                c.getTitle(),
                c.getSlug(),
                c.getShortDescription(),
                c.getDescription(),
                c.getPriceCents(),
                c.getCurrency(),
                c.getLevel(),
                c.getIsActive(),
                c.getThumbnailUrl()
            );
        }
    }
}
