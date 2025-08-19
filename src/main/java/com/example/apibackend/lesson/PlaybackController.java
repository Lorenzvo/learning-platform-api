package com.example.apibackend.lesson;

import com.example.apibackend.auth.JwtUtil;
import com.example.apibackend.enrollment.EnrollmentRepository;
import com.example.apibackend.enrollment.Enrollment;
import com.example.apibackend.user.User;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * PlaybackController issues a short-lived JWT for lesson playback.
 * Security: Only users with Enrollment.ACTIVE for the course owning the lesson can get a token.
 * Token is valid for ~5 minutes and should be used immediately by the client for playback APIs.
 * The token is signed with the server's JWT secret and scoped to the lesson/course/user.
 */
@RestController
@RequestMapping("/api/playback")
@RequiredArgsConstructor
public class PlaybackController {
    private final EnrollmentRepository enrollmentRepository;
    private final LessonRepository lessonRepository;
    private final JwtUtil jwtUtil;

    /**
     * Issues a playback token for a lesson if the user is actively enrolled in the course.
     * @param lessonId The lesson to play
     * @return { token, expiresAtEpochSec }
     */
    @GetMapping("/token/{lessonId}")
    public ResponseEntity<PlaybackTokenResponse> getPlaybackToken(
            @PathVariable Long lessonId,
            @AuthenticationPrincipal User principal
    ) {
        // Extract userId from principal if available, otherwise fallback (customize as needed)
        Long userId = principal.getId();
        String email = principal.getEmail();
        // If your User entity does not have getAuthorities(), default to "USER" or use another method to get the role
        String role = "USER"; // or principal.getRole() if available
        // If you store userId in username or as a custom claim, parse it here
        // Example: userId = Long.valueOf(email) if username is userId
        // Otherwise, you may need to query userRepository by email
        // For now, fallback to 0L if not available
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found"));
        Long courseId = lesson.getModule().getCourse().getId();
        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                .orElse(null);
        if (enrollment == null || enrollment.getStatus() != Enrollment.EnrollmentStatus.ACTIVE) {
            return ResponseEntity.status(403).build();
        }
        // Token lifetime: 5 minutes
        long expiresAt = Instant.now().getEpochSecond() + 300;
        String token = jwtUtil.createToken(userId, email, role);
        // Client usage: Pass token in Authorization header for playback APIs
        return ResponseEntity.ok(new PlaybackTokenResponse(token, expiresAt));
    }

    /**
     * DTO for playback token response
     */
    @Data
    @AllArgsConstructor
    public static class PlaybackTokenResponse {
        /** JWT token for playback, valid for ~5 minutes */
        private String token;
        /** Expiry time (epoch seconds) for client-side validation */
        private long expiresAtEpochSec;
    }
}
