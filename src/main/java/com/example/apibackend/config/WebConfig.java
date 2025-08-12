package com.example.apibackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration.
 * - Enables CORS for the React dev server (http://localhost:5173) to call the API on :8080.
 * - Keep this permissive in dev; tighten origins/methods/headers for production.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")                         // Apply CORS to all endpoints
                .allowedOrigins("http://localhost:5173")      // Vite dev server origin
                .allowedMethods("GET","POST","PUT","PATCH","DELETE","OPTIONS") // Methods allowed
                .allowCredentials(true);                      // Allow cookies/credentials if you use them
        // .allowedHeaders("*")                       // Uncomment to allow custom headers explicitly
        // .exposedHeaders("X-Total-Count")           // Example: expose pagination headers to the browser
    }
}
