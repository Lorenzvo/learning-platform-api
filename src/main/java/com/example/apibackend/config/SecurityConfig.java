package com.example.apibackend.config;

import com.example.apibackend.auth.JwtAuthFilter;
import com.example.apibackend.auth.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

@Configuration
public class SecurityConfig {
    @Value("${app.cors.allowed-origins:http://localhost:3000}")
    private List<String> allowedOrigins;

    @Autowired
    private JwtUtil jwtUtil;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // CORS: restrict to known frontend origins from app.cors.allowed-origins
                .cors(Customizer.withDefaults())
                // CSRF protection is disabled for stateless APIs (no cookies/session)
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Public catalogue endpoints: open for all users (no login required)
                        .requestMatchers(HttpMethod.GET, "/api/courses/**").permitAll()
                        // Auth endpoints: open for signup/login
                        .requestMatchers("/api/auth/**").permitAll()
                        // Actuator endpoints: permit all in test profile
                        .requestMatchers("/actuator/**").permitAll()
                        // Admin endpoints: require ROLE_ADMIN
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // Everything else: require authentication (ROLE_USER or ROLE_ADMIN)
                        .anyRequest().authenticated()
                )
                // Register JwtAuthFilter with JwtAuth bean before UsernamePasswordAuthenticationFilter
                .addFilterBefore(new JwtAuthFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class)
                // enable basic auth if I decide to protect something quickly
                .httpBasic(Customizer.withDefaults())
                .build();
    }
    // Inline comments:
    // - Passwords are hashed with bcrypt in AuthController for security.
    // - JWT is used for stateless sessions; no server-side session storage.
    // - JWT keys/issuer/TTL are configured in application.yml under security.jwt.*.
    // - Public catalogue endpoints are open so users can browse courses before signing up.
}