package com.example.apibackend.enrollment;

import com.example.apibackend.course.Course;
import com.example.apibackend.course.CourseRepository;
import com.example.apibackend.user.User;
import com.example.apibackend.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.Optional;

/**
 * DEV-ONLY ENDPOINT: Allows direct enrollment creation for testing and local development.
 * This bypasses payment and business logic, and should NEVER be enabled in production.
 * In production, enrollment creation will be handled by payment webhook logic.
 * Controlled by app.devEndpoints.enabled property and active only in 'dev' profile.
 */
@RestController
@RequestMapping("/api/dev/enrollments")
@Profile("dev")
public class DevEnrollmentController {
    @Value("${app.devEndpoints.enabled:false}")
    private boolean devEndpointsEnabled;

    private final EnrollmentRepository enrollmentRepo;
    private final UserRepository userRepo;
    private final CourseRepository courseRepo;

    public DevEnrollmentController(
        EnrollmentRepository enrollmentRepo,
        UserRepository userRepo,
        CourseRepository courseRepo
    ) {
        this.enrollmentRepo = enrollmentRepo;
        this.userRepo = userRepo;
        this.courseRepo = courseRepo;
    }

    @PostMapping
    public ResponseEntity<?> createDevEnrollment(@RequestBody DevEnrollmentRequest req) {
        if (!devEndpointsEnabled) {
            return ResponseEntity.notFound().build();
        }
        // Validate user
        Optional<User> userOpt = userRepo.findById(req.userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found");
        }
        // Validate course
        Optional<Course> courseOpt = courseRepo.findById(req.courseId);
        if (courseOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Course not found");
        }
        // Check for existing enrollment
        Optional<Enrollment> existing = enrollmentRepo.findByUserIdAndCourseId(req.userId, req.courseId);
        if (existing.isPresent()) {
            return ResponseEntity.status(409).body("Enrollment already exists");
        }
        // Create new enrollment
        Enrollment enrollment = new Enrollment();
        enrollment.setUser(userOpt.get());
        enrollment.setCourse(courseOpt.get());
        enrollment.setStatus(Enrollment.Status.ACTIVE);
    //  enrollment.setEnrolledAt(Instant.now());
        enrollmentRepo.save(enrollment);
        return ResponseEntity.status(201).body(new DevEnrollmentDto(enrollment));
    }

    /**
     * Simple DTO for dev enrollment response
     */
    public static class DevEnrollmentDto {
        public final Long id;
        public final Long userId;
        public final Long courseId;
        public final String status;
 //       public final Instant enrolledAt;
        public DevEnrollmentDto(Enrollment e) {
            this.id = e.getId();
            this.userId = e.getUser().getId();
            this.courseId = e.getCourse().getId();
            this.status = e.getStatus().toString();
      //      this.enrolledAt = e.getEnrolledAt();
        }
    }

    /**
     * Request body for dev enrollment creation
     */
    public static class DevEnrollmentRequest {
        public Long userId;
        public Long courseId;
    }
}
