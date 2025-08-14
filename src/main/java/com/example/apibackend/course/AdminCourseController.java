package com.example.apibackend.course;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Stub admin endpoint for security testing.
 */
@RestController
@RequestMapping("/api/admin/courses")
public class AdminCourseController {
    @GetMapping
    public ResponseEntity<?> getAdminCourses() {
        // Stub: just return 200 OK for security test
        return ResponseEntity.ok().build();
    }
}

