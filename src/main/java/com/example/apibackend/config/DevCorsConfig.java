package com.example.apibackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Profile("dev")
@Configuration
public class DevCorsConfig {
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173")); // Allow Vite dev server
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE")); // Allow common HTTP methods
        configuration.setAllowedHeaders(List.of("Content-Type", "Authorization")); // Allow Content-Type and Authorization headers
        configuration.setExposedHeaders(List.of("Authorization", "Location")); // Expose Authorization and Location headers to frontend
        configuration.setAllowCredentials(true); // Allow cookies/credentials if needed
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
