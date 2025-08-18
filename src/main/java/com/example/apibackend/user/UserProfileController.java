package com.example.apibackend.user;

import com.example.apibackend.enrollment.EnrollmentRepository;
import com.example.apibackend.auth.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.context.annotation.Bean;
import java.time.Instant;

@RestController
@RequestMapping("/api/users/me")
public class UserProfileController {
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private EnrollmentRepository enrollmentRepo;
    @Autowired
    private JwtUtil jwtUtil;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // Helper: get current user from JWT
    private User getCurrentUser() {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepo.findByEmail(email).orElse(null);
    }

    /**
     * GET /api/users/me
     * Returns current user's profile info. Never exposes password hash.
     * Role/email cannot be updated here for security reasons.
     */
    @GetMapping
    public ResponseEntity<UserProfileDto> getProfile() {
        User user = getCurrentUser();
        if (user == null) return ResponseEntity.status(401).build();
        long enrolledCourseCount = enrollmentRepo.countByUserId(user.getId());
        UserProfileDto dto = new UserProfileDto();
        dto.id = user.getId();
        dto.email = user.getEmail();
        dto.name = null; // Add name field if present in User entity
        dto.role = user.getRole();
        dto.createdAt = user.getCreatedAt();
        dto.enrolledCourseCount = enrolledCourseCount;
        return ResponseEntity.ok(dto);
    }

    /**
     * PUT /api/users/me
     * Allows updating only the name field. Validates length. Does not allow role/email changes here.
     */
    @PutMapping
    public ResponseEntity<UserProfileDto> updateProfile(@RequestBody UpdateProfileRequest req) {
        User user = getCurrentUser();
        if (user == null) return ResponseEntity.status(401).build();
        // Only allow name update
        if (req.name != null && req.name.length() >= 2 && req.name.length() <= 50) {
            // If you have a name field in User, set it here
            // user.setName(req.name);
            userRepo.save(user);
        }
        long enrolledCourseCount = enrollmentRepo.countByUserId(user.getId());
        UserProfileDto dto = new UserProfileDto();
        dto.id = user.getId();
        dto.email = user.getEmail();
        dto.name = req.name; // Or user.getName() if present
        dto.role = user.getRole();
        dto.createdAt = user.getCreatedAt();
        dto.enrolledCourseCount = enrolledCourseCount;
        return ResponseEntity.ok(dto);
    }

    /**
     * PUT /api/users/me/password
     * Allows user to change password. Verifies current password via bcrypt, then updates.
     * Never allows role/email changes here for security.
     */
    @PutMapping("/password")
    public ResponseEntity<Void> updatePassword(@RequestBody UpdatePasswordRequest req) {
        User user = getCurrentUser();
        if (user == null) return ResponseEntity.status(401).build();
        if (!passwordEncoder.matches(req.currentPassword, user.getPasswordHash())) {
            return ResponseEntity.status(403).build();
        }
        user.setPasswordHash(passwordEncoder.encode(req.newPassword));
        userRepo.save(user);
        return ResponseEntity.noContent().build();
    }

    @Data
    public static class UserProfileDto {
        public Long id;
        public String email;
        public String name;
        public String role;
        public Instant createdAt;
        public long enrolledCourseCount;
    }
    @Data
    public static class UpdateProfileRequest {
        @Size(min = 2, max = 50)
        public String name;
    }
    @Data
    public static class UpdatePasswordRequest {
        @NotBlank public String currentPassword;
        @NotBlank public String newPassword;
    }
}
