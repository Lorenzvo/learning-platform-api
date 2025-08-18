package com.example.apibackend.course;

import com.example.apibackend.lesson.Lesson;
import com.example.apibackend.module.Module;
import com.example.apibackend.lesson.LessonDto;
import com.example.apibackend.module.ModuleDto;
import com.example.apibackend.module.ModuleRepository;
import com.example.apibackend.lesson.LessonRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.nio.charset.StandardCharsets;

import java.util.List;
import java.util.stream.Collectors;
import com.example.apibackend.enrollment.EnrollmentRepository;
import com.example.apibackend.enrollment.Enrollment;

/**
 * Public course catalog endpoints.
 * - GET /api/courses: list active courses
 * - GET /api/courses/{slug}: fetch an active course by slug
 */

@RestController
@RequestMapping("/api/courses")
@Validated // Enables bean validation on query params
public class CourseController {

    private final CourseRepository repo;
    private final ModuleRepository moduleRepo; // Inject module repository
    private final LessonRepository lessonRepo; // Inject lesson repository
    private final EnrollmentRepository enrollmentRepo;

    // Constructor injection for all repositories
    public CourseController(CourseRepository repo, ModuleRepository moduleRepo, LessonRepository lessonRepo, EnrollmentRepository enrollmentRepo) {
        this.repo = repo;
        this.moduleRepo = moduleRepo;
        this.lessonRepo = lessonRepo;
        this.enrollmentRepo = enrollmentRepo;
    }

    /**
     * GET /api/courses/{slug}
     * Fetches course, then loads modules and lessons via repositories (avoids N+1 and entity coupling).
     * Maps to DTOs to avoid lazy loading issues and leaking internal fields.
     */

    @GetMapping("/{slug}")
    public ResponseEntity<CourseDetailDto> getCourseDetail(@PathVariable String slug) {
        return repo.findBySlugAndIsActiveTrue(slug)
                .map(course -> {
                    // Fetch instructor summary
                    InstructorRepository instructorRepo = com.example.apibackend.LearningPlatformApiApplication.getBean(InstructorRepository.class);
                    var instructor = instructorRepo.findById(course.getInstructorId()).orElse(null);
                    InstructorController.InstructorSummaryDto instructorDto = instructor != null ? InstructorController.InstructorSummaryDto.fromEntity(instructor) : null;
                    // Fetch all modules for this course, ordered by position
                    List<Module> modules = moduleRepo.findByCourseIdOrderByPositionAsc(course.getId());
                    List<ModuleDto> moduleDtos = modules.stream().map(module -> {
                        List<Lesson> lessons = lessonRepo.findByModuleIdOrderByIdAsc(module.getId());
                        List<LessonDto> lessonDtos = lessons.stream().map(lesson -> new LessonDto(
                                lesson.getId(),
                                lesson.getTitle(),
                                lesson.getType().name(),
                                lesson.getDurationSeconds(),
                                lesson.isDemo()
                        )).collect(Collectors.toList());
                        return new ModuleDto(
                                module.getId(),
                                module.getTitle(),
                                module.getPosition(),
                                lessonDtos
                        );
                    }).collect(Collectors.toList());
                    // Fetch reviews summary
                    ReviewRepository reviewRepo = com.example.apibackend.LearningPlatformApiApplication.getBean(ReviewRepository.class);
                    Double avgRating = reviewRepo.findAverageRatingByCourseId(course.getId());
                    long reviewCount = reviewRepo.countByCourseId(course.getId());
                    // Assemble the course detail DTO
                    return new CourseDetailDto(
                            course.getTitle(),
                            course.getPriceCents(),
                            course.getSlug(),
                            course.getLevel(),
                            course.getDescription(),
                            course.getThumbnailUrl(),
                            course.getIsActive(),
                            moduleDtos,
                            instructorDto,
                            avgRating != null ? avgRating : 0.0,
                            reviewCount
                    );
                })
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/courses with pagination, sorting, and optional filters.
     * Handles any combination of query params (page, size, sort, q, level, published).
     * Returns a page of CourseSummaryDto, not JPA entities, for API safety and decoupling.
     * If no pagination/filter params are provided, returns all active courses as a single page.
     *
     * Refactored: Removed strict param matching so partial params work (Spring will always call this method for /api/courses).
     */

    @GetMapping
    public Page<CourseSummaryDto> searchCourses(
            @PageableDefault(page = 0, size = 12)
            @SortDefault.SortDefaults({@SortDefault(sort = "title")}) Pageable pageable,
            @RequestParam(required = false) @Size(max = 100) String q,
            @RequestParam(required = false) @Size(max = 20) String level,
            @RequestParam(required = false) Boolean published
    ) {
        // If any filter or pagination param is present, use repository search
        if (q != null || level != null || published != null || pageable.getPageSize() != 12 || pageable.getPageNumber() != 0) {
            Page<Course> page = repo.search(q, level, published, pageable);
            // Map entities to DTOs for API safety (never expose JPA entities directly)
            return page.map(c -> new CourseSummaryDto(
                    c.getId(),
                    c.getTitle(),
                    c.getSlug(),
                    c.getDescription() != null ? c.getDescription().substring(0, Math.min(80, c.getDescription().length())) : null,
                    c.getPriceCents(),
                    c.getLevel(),
                    c.getIsActive()
            ));
        } else {
            // No filters/pagination: return all active courses as a single page
            List<CourseSummaryDto> dtos = repo.findAll().stream()
                    .filter(c -> Boolean.TRUE.equals(c.getIsActive()))
                    .map(c -> new CourseSummaryDto(
                            c.getId(),
                            c.getTitle(),
                            c.getSlug(),
                            c.getDescription() != null ? c.getDescription().substring(0, Math.min(80, c.getDescription().length())) : null,
                            c.getPriceCents(),
                            c.getLevel(),
                            c.getIsActive()
                    )).toList();
            // Wrap in a single-page Page object
            return new org.springframework.data.domain.PageImpl<>(dtos);
        }
    }

    @Value("${app.secret:defaultSecret}")
    private String appSecret;

    /**
     * GET /api/courses/{courseId}/demo/{lessonId}
     * Returns a signed token for demo lesson playback if valid.
     * Never expose raw media URLs directly! Instead, issue a short-lived token that can be validated by a media gateway or CDN.
     * In production, this would be replaced with a signed CDN/S3 URL for secure, time-limited access.
     */

    @GetMapping("/{courseId}/demo/{lessonId}")
    public ResponseEntity<?> getDemoLessonToken(@PathVariable Long courseId, @PathVariable Long lessonId) {
        // Find lesson and verify it belongs to the course
        Lesson lesson = lessonRepo.findById(lessonId).orElse(null);
        // Validate lesson existence and association with a valid module/course
        if (lesson == null) {
            return ResponseEntity.notFound().build();
        }
        Module module = lesson.getModule();
        if (module == null || module.getCourse() == null || !courseId.equals(module.getCourse().getId())) {
            return ResponseEntity.notFound().build();
        }
        if (!lesson.isDemo()) {
            return ResponseEntity.status(403).body("Demo access not allowed for this lesson.");
        }
        // Generate expiry (10 min from now)
        Instant expiresAt = Instant.now().plus(10, ChronoUnit.MINUTES);
        String payload = lessonId + ":" + expiresAt.getEpochSecond();
        String token = signHmac(payload, appSecret);
        // Never expose raw media URLs! Instead, issue a token for secure access.
        // In production, use a CDN/S3 signed URL here.
        return ResponseEntity.ok(new DemoTokenResponse(token, expiresAt.toString()));
    }

    /**
     * GET /api/courses/{courseId}/lessons/{lessonId}/playback
     * Checks user's ACTIVE enrollment, verifies lesson belongs to course and is not a demo,
     * and returns a short-lived signed token (HMAC or pre-signed URL stub).
     */
    @GetMapping("/{courseId}/lessons/{lessonId}/playback")
    public ResponseEntity<?> getLessonPlaybackToken(
            @PathVariable Long courseId,
            @PathVariable Long lessonId,
            @AuthenticationPrincipal com.example.apibackend.user.User user
    ) {
        // Check enrollment
        var enrollmentOpt = enrollmentRepo.findByUserIdAndCourseId(user.getId(), courseId);
        var enrollment = enrollmentOpt.orElse(null);
        if (enrollment == null || enrollment.getStatus() != Enrollment.EnrollmentStatus.ACTIVE) {
            return ResponseEntity.status(403).body("No active enrollment");
        }
        // Check lesson
        Lesson lesson = lessonRepo.findById(lessonId).orElse(null);
        if (lesson == null || lesson.isDemo()) {
            return ResponseEntity.status(403).body("Lesson not available for playback");
        }
        Module module = lesson.getModule();
        if (module == null || module.getCourse() == null || !courseId.equals(module.getCourse().getId())) {
            return ResponseEntity.status(403).body("Lesson does not belong to course");
        }
        // Generate short-lived token (HMAC stub)
        Instant expiresAt = Instant.now().plus(10, ChronoUnit.MINUTES);
        String payload = lessonId + ":" + user.getId() + ":" + expiresAt.getEpochSecond();
        String token = signHmac(payload, appSecret);
        return ResponseEntity.ok(new DemoTokenResponse(token, expiresAt.toString()));
    }

    // HMAC signing helper
    private String signHmac(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hmac) + "." + Base64.getUrlEncoder().withoutPadding().encodeToString(data.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("Failed to sign token", e);
        }
    }

    // DTO for demo token response
    public static class DemoTokenResponse {
        public final String token;
        public final String expiresAt;
        public DemoTokenResponse(String token, String expiresAt) {
            this.token = token;
            this.expiresAt = expiresAt;
        }
    }

    /**
     * GET /api/courses/{slug}/preview
     * Returns only demo lessons for the course (is_demo=true).
     */
    @GetMapping("/{slug}/preview")
    public ResponseEntity<List<LessonDto>> getCoursePreview(@PathVariable String slug) {
        var course = repo.findBySlugAndIsActiveTrue(slug).orElse(null);
        if (course == null) return ResponseEntity.notFound().build();
        // Fetch all modules for this course
        List<Module> modules = moduleRepo.findByCourseId(course.getId());
        List<Long> moduleIds = new java.util.ArrayList<>();
        for (Module m : modules) moduleIds.add(m.getId());
        // Fetch demo lessons for these modules
        List<Lesson> demoLessons = moduleIds.isEmpty() ? java.util.Collections.emptyList() : lessonRepo.findByModuleIdInAndIsDemoTrue(moduleIds);
        List<LessonDto> dtos = new java.util.ArrayList<>();
        for (Lesson lesson : demoLessons) dtos.add(new LessonDto(
            lesson.getId(),
            lesson.getTitle(),
            lesson.getType().name(),
            lesson.getDurationSeconds(),
            lesson.isDemo()
        ));
        return ResponseEntity.ok(dtos);
    }
}