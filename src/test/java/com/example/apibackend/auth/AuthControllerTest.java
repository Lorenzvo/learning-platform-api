package com.example.apibackend.auth;

import com.example.apibackend.user.User;
import com.example.apibackend.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for AuthController.
 *
 * - signup_hashesPasswordAndSavesUser: Verifies that signup hashes the password using bcrypt and saves the user with correct fields.
 * - login_validCredentials_returnsJwtToken: Verifies that login with valid credentials returns a JWT token.
 * - login_invalidPassword_returnsUnauthorized: Verifies that login with wrong password returns 401 Unauthorized.
 * - login_userNotFound_returnsUnauthorized: Verifies that login with non-existent user returns 401 Unauthorized.
 *
 * Passwords are hashed for security (stateless sessions, no server-side session storage).
 * JWT keys/issuer/TTL are configured in application.yml under security.jwt.*.
 * Public catalogue endpoints are open so users can browse courses before signing up.
 */

class AuthControllerTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtUtil jwtUtil;
    @InjectMocks
    private AuthController authController;
    private MockMvc mockMvc;
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    void signup_hashesPasswordAndSavesUser() throws Exception {
        String rawPassword = "password123";
        String email = "test@example.com";
        String role = "USER";
        String json = String.format("{\"email\":\"%s\",\"password\":\"%s\",\"role\":\"%s\"}", email, rawPassword, role);

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(passwordEncoder.matches(rawPassword, savedUser.getPasswordHash())).isTrue();
        assertThat(savedUser.getEmail()).isEqualTo(email);
        assertThat(savedUser.getRole()).isEqualTo(role);
    }

    @Test
    void login_validCredentials_returnsJwtToken() throws Exception {
        String rawPassword = "password123";
        String hashedPassword = passwordEncoder.encode(rawPassword);
        String email = "test@example.com";
        String role = "USER";
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(hashedPassword);
        user.setRole(role);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jwtUtil.createToken(user.getId(), user.getEmail(), user.getRole())).thenReturn("jwt-token");

        String json = String.format("{\"email\":\"%s\",\"password\":\"%s\"}", email, rawPassword);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }


    @Test
    void login_invalidPassword_returnsUnauthorized() throws Exception {
        String rawPassword = "password123";
        String wrongPassword = "wrongpass";
        String hashedPassword = passwordEncoder.encode(rawPassword);
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(hashedPassword);
        user.setRole("USER");

        // Simulate user found in DB
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        String json = String.format("{\"email\":\"%s\",\"password\":\"%s\"}", email, wrongPassword);

        // Should fail authentication and not issue JWT
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_userNotFound_returnsUnauthorized() throws Exception {
        String email = "notfound@example.com";
        // Simulate user not found in DB
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        String json = String.format("{\"email\":\"%s\",\"password\":\"irrelevant\"}", email);

        // Should fail authentication and not issue JWT
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isUnauthorized());
    }
}
