package com.example.apibackend.auth;

import com.example.apibackend.user.User;
import com.example.apibackend.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.apibackend.email.EmailService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final UserRepository userRepo;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final PasswordResetTokenRepository passwordResetTokenRepo;
    @Autowired
    private EmailService emailService;

    public AuthController(UserRepository userRepo, JwtUtil jwtUtil, PasswordResetTokenRepository passwordResetTokenRepo) {
        this.userRepo = userRepo;
        this.jwtUtil = jwtUtil;
        this.passwordResetTokenRepo = passwordResetTokenRepo;
    }

    /**
     * Signup endpoint: hashes password with bcrypt and saves user.
     * Password hashing prevents storing plain text passwords and protects against leaks.
     */

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest req) {
        if (userRepo.existsByEmail(req.email)) {
            log.info("Signup attempt with existing email: {}", req.email);
            return ResponseEntity.status(409).body("Email already exists");
        }
        User user = new User();
        user.setEmail(req.email);
        String hashed = passwordEncoder.encode(req.password);
        user.setPasswordHash(hashed);
        user.setRole("USER");
        userRepo.save(user);
        log.info("User signed up: email={}, hash={}", req.email, hashed);
        return ResponseEntity.status(201).build();
    }

    /**
     * Login endpoint: verifies password hash and issues JWT for stateless session.
     * Stateless JWT means no server-side session storage; all info is in the token.
     */

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        User user = userRepo.findByEmail(req.email).orElse(null);
        log.info("Login attempt: email={}, foundUser={}, hash={}", req.email, user != null, user != null ? user.getPasswordHash() : "null");
        if (user == null || !passwordEncoder.matches(req.password, user.getPasswordHash())) {
            log.warn("Invalid credentials for email: {}", req.email);
            return ResponseEntity.status(401).body("Invalid credentials");
        }
        String token = jwtUtil.createToken(user.getId(), user.getEmail(), user.getRole());
        log.info("Login success: email={}, userId={}, role={}", user.getEmail(), user.getId(), user.getRole());
        return ResponseEntity.ok(new JwtResponse(token));
    }

    /**
     * POST /api/auth/forgot-password
     * Always returns 204 to avoid user enumeration. If user exists, creates a single-use token (TTL=15min), logs reset link.
     * Security: No indication if email exists. Token is random, single-use, expires quickly.
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestBody ForgotPasswordRequest req) {
        User user = userRepo.findByEmail(req.email).orElse(null);
        if (user != null) {
            // Only allow one valid token per user at a time (business logic)
            LocalDateTime now = LocalDateTime.now();
            long validTokens = passwordResetTokenRepo.countByUserIdAndUsedFalseAndExpiresAtAfter(user.getId(), now);
            if (validTokens == 0) {
                String token = generateToken();
                PasswordResetToken prt = new PasswordResetToken();
                prt.setUser(user);
                prt.setToken(token);
                prt.setExpiresAt(now.plusMinutes(15));
                prt.setUsed(false);
                passwordResetTokenRepo.save(prt);
                // Log the reset link (stub for email)
                String resetLink = "https://your-frontend/reset?token=" + token;
                log.info("[PasswordReset] Sent password reset link to {}: {}", user.getEmail(), resetLink);
                emailService.sendEnrollmentConfirmation(user, null); // Optionally, send a stub email
            }
        }
        // Always return 204 to avoid leaking account existence
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/auth/reset-password
     * Verifies token, sets new password (bcrypt), marks token used. Returns 204 on success or failure (no info leak).
     * Security: Token must be unexpired and unused. Password is hashed.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody ResetPasswordRequest req) {
        PasswordResetToken prt = passwordResetTokenRepo.findByToken(req.token).orElse(null);
        LocalDateTime now = LocalDateTime.now();
        if (prt != null && !prt.isUsed() && prt.getExpiresAt().isAfter(now)) {
            User user = prt.getUser();
            user.setPasswordHash(passwordEncoder.encode(req.newPassword));
            userRepo.save(user);
            prt.setUsed(true);
            passwordResetTokenRepo.save(prt);
            log.info("[PasswordReset] Password reset for user {} via token {}", user.getEmail(), req.token);
        }
        // Always return 204 to avoid leaking info
        return ResponseEntity.noContent().build();
    }

    // Helper: generate a secure random token (32 bytes hex)
    private String generateToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    public static class SignupRequest {
        @NotBlank public String email;
        @NotBlank public String password;
    }
    public static class LoginRequest {
        @NotBlank public String email;
        @NotBlank public String password;
    }
    public static class JwtResponse {
        public final String token;
        public JwtResponse(String token) { this.token = token; }
    }
    public static class ForgotPasswordRequest {
        @NotBlank public String email;
    }
    public static class ResetPasswordRequest {
        @NotBlank public String token;
        @NotBlank public String newPassword;
    }
}
