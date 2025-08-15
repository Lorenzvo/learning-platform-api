package com.example.apibackend.auth;

import com.example.apibackend.user.User;
import com.example.apibackend.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository userRepo;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthController(UserRepository userRepo, JwtUtil jwtUtil) {
        this.userRepo = userRepo;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Signup endpoint: hashes password with bcrypt and saves user.
     * Password hashing prevents storing plain text passwords and protects against leaks.
     */

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest req) {
        if (userRepo.existsByEmail(req.email)) {
            return ResponseEntity.status(409).body("Email already exists");
        }
        User user = new User();
        user.setEmail(req.email);
        user.setPasswordHash(passwordEncoder.encode(req.password));
        user.setRole("USER");
        userRepo.save(user);
        return ResponseEntity.status(201).build();
    }

    /**
     * Login endpoint: verifies password hash and issues JWT for stateless session.
     * Stateless JWT means no server-side session storage; all info is in the token.
     */

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        User user = userRepo.findByEmail(req.email).orElse(null);
        if (user == null || !passwordEncoder.matches(req.password, user.getPasswordHash())) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
        String token = jwtUtil.createToken(user.getId(), user.getEmail(), user.getRole());
        return ResponseEntity.ok(new JwtResponse(token));
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
}

