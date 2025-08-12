package com.example.apibackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // !! When calling from a browser frontend later, let Spring Security use WebMvc CORS:
                .cors(Customizer.withDefaults())
                // CSRF mainly blocks state-changing requests from browsers; leave it disabled for API dev.
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // health should be open
                        .requestMatchers("/actuator/health").permitAll()
                        // open public catalog endpoints
                        .requestMatchers(HttpMethod.GET, "/api/courses/**").permitAll()
                        // everything else open for now (switch to .authenticated() later)
                        .anyRequest().permitAll()
                )
                // enable basic auth if I decide to protect something quickly
                .httpBasic(Customizer.withDefaults())
                .build();
    }
}