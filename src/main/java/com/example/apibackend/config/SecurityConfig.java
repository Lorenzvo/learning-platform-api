package com.example.apibackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

@Configuration
public class SecurityConfig {
    @Value("${app.cors.allowed-origins:http://localhost:3000}")
    private List<String> allowedOrigins;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // !! When calling from a browser frontend later, let Spring Security use WebMvc CORS:
                .cors(Customizer.withDefaults())
                // CSRF mainly blocks state-changing requests from browsers; leave it disabled for API dev.
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // open up actuator endpoints for health checks/metrics
                        .requestMatchers("/actuator/**").permitAll()
                        // open public catalog endpoints
                        .requestMatchers(HttpMethod.GET, "/api/courses/**").permitAll()
                        // open demo lesson token endpoint for GET
                        .requestMatchers(HttpMethod.GET, "/api/courses/*/demo/*").permitAll()
                        // dev endpoints are only enabled in 'dev' profile and controlled by a flag, but enable for now for local development
                        .requestMatchers("/api/dev/**").permitAll()
                        // admin endpoints require admin role
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // everything else must be authenticated
                        .anyRequest().authenticated()
                )
                // Stateless JWT authentication would be configured here (stubbed for now)
                // .oauth2ResourceServer().jwt() ...
                // CSRF protection is disabled for stateless APIs (no cookies/session)
                .csrf(csrf -> csrf.disable())
                // CORS: restrict to known frontend origins from app.cors.allowed-origins
                .cors(cors -> cors.configurationSource(request -> {
                    org.springframework.web.cors.CorsConfiguration config = new org.springframework.web.cors.CorsConfiguration();
                    if (allowedOrigins != null) {
                        allowedOrigins.forEach(config::addAllowedOrigin);
                    }
                    config.addAllowedMethod("*");
                    config.addAllowedHeader("*");
                    config.setAllowCredentials(true);
                    return config;
                }))
                // enable basic auth if I decide to protect something quickly
                .httpBasic(Customizer.withDefaults())
                .build();
    }
    // Annotated comments:
    // - Stateless JWT authentication is recommended for APIs; session/cookie auth is not used here. See .oauth2ResourceServer().jwt() for real implementation.
    // - CSRF is disabled because stateless APIs do not use cookies or browser sessions.
    // - CORS is scoped to known frontend origins for security; never use '*' in production. Load allowed origins from config.
}