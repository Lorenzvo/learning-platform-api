package com.example.apibackend.auth;

import com.example.apibackend.email.EmailService;
import com.example.apibackend.user.User;
import com.example.apibackend.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final UserRepository userRepo;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final PasswordResetTokenRepository passwordResetTokenRepo;
    private final RefreshTokenRepository refreshTokenRepo;
    private final EmailService emailService;

    public AuthController(UserRepository userRepo, JwtUtil jwtUtil, PasswordResetTokenRepository passwordResetTokenRepo, RefreshTokenRepository refreshTokenRepo, EmailService emailService) {
        this.userRepo = userRepo;
        this.jwtUtil = jwtUtil;
        this.passwordResetTokenRepo = passwordResetTokenRepo;
        this.refreshTokenRepo = refreshTokenRepo;
        this.emailService = emailService;
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
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req, HttpServletResponse response) {
        User user = userRepo.findByEmailAndDeletedAtIsNull(req.email).orElse(null);
        log.info("Login attempt: email={}, foundUser={}, hash={}", req.email, user != null, user != null ? user.getPasswordHash() : "null");
        if (user == null || !passwordEncoder.matches(req.password, user.getPasswordHash())) {
            log.warn("Invalid credentials for email: {}", req.email);
            return ResponseEntity.status(401).body("Invalid credentials");
        }
        String token = jwtUtil.createToken(user.getId(), user.getEmail(), user.getRole());
        // Generate refresh token
        String refreshToken = generateToken();
        Instant expiresAt = Instant.now().plusSeconds(7 * 24 * 60 * 60); // 7 days
        refreshTokenRepo.save(new RefreshToken(refreshToken, user.getId(), expiresAt));
        // Set refresh token as HTTP-only cookie
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60);
        cookie.setSecure(true); // Required for SameSite=None on HTTPS
        response.addCookie(cookie);
        // Manually set SameSite=None using header (since Cookie.setSameSite is not available)
        response.addHeader("Set-Cookie", String.format(
            "refreshToken=%s; Max-Age=%d; Path=/; HttpOnly; Secure; SameSite=None",
            refreshToken, 7 * 24 * 60 * 60
        ));
        log.info("Login success: email={}, userId={}, role={}", user.getEmail(), user.getId(), user.getRole());
        return ResponseEntity.ok(new JwtResponse(token));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return ResponseEntity.status(401).body("No refresh token");
        String refreshToken = null;
        for (Cookie c : cookies) {
            if ("refreshToken".equals(c.getName())) {
                refreshToken = c.getValue();
                break;
            }
        }
        if (refreshToken == null) return ResponseEntity.status(401).body("No refresh token");
        RefreshToken tokenEntity = refreshTokenRepo.findByToken(refreshToken).orElse(null);
        if (tokenEntity == null || tokenEntity.getExpiresAt().isBefore(Instant.now())) {
            return ResponseEntity.status(401).body("Invalid or expired refresh token");
        }
        User user = userRepo.findById(tokenEntity.getUserId()).orElse(null);
        if (user == null) return ResponseEntity.status(401).body("User not found");
        String newAccessToken = jwtUtil.createToken(user.getId(), user.getEmail(), user.getRole());
        // Optionally rotate refresh token
        String newRefreshToken = generateToken();
        tokenEntity.setToken(newRefreshToken);
        tokenEntity.setExpiresAt(Instant.now().plusSeconds(7 * 24 * 60 * 60));
        refreshTokenRepo.save(tokenEntity);
        Cookie cookie = new Cookie("refreshToken", newRefreshToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60);
        cookie.setSecure(true); // Required for SameSite=None on HTTPS
        response.addCookie(cookie);
        // Manually set SameSite=None using header (since Cookie.setSameSite is not available)
        response.addHeader("Set-Cookie", String.format(
            "refreshToken=%s; Max-Age=%d; Path=/; HttpOnly; Secure; SameSite=None",
            newRefreshToken, 7 * 24 * 60 * 60
        ));
        return ResponseEntity.ok(new JwtResponse(newAccessToken));
    }

    /**
     * POST /api/auth/forgot-password
     * Always returns 204 to avoid user enumeration. If user exists, creates a single-use token (TTL=15min), logs reset link.
     * Security: No indication if email exists. Token is random, single-use, expires quickly.
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestBody ForgotPasswordRequest req) {
        // Only allow password reset for users who are not soft-deleted
        User user = userRepo.findByEmailAndDeletedAtIsNull(req.email).orElse(null);
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

    /**
     * Logout endpoint: for JWT, just return success (client deletes token). Optionally log event.
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("refreshToken".equals(c.getName())) {
                    refreshTokenRepo.deleteByToken(c.getValue());
                    // Clear cookie
                    Cookie clearCookie = new Cookie("refreshToken", "");
                    clearCookie.setHttpOnly(true);
                    clearCookie.setPath("/");
                    clearCookie.setMaxAge(0);
                    response.addCookie(clearCookie);
                }
            }
        }
        return ResponseEntity.ok().build();
    }

    // Helper: generate a secure random token (32 bytes hex)
    private String generateToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
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
