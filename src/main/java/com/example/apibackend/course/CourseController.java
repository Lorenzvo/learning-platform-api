package com.example.apibackend.course;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Public course catalog endpoints.
 * - GET /api/courses: list active courses
 * - GET /api/courses/{slug}: fetch an active course by slug
 */

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseRepository repo;

    // Constructor injection (preferred): Spring provides the repo at runtime
    public CourseController(CourseRepository repo) { this.repo = repo; }

    @GetMapping
    public List<Course> listActive() {
        // Simple approach: fetch all and filter active in memory.
        // For large catalogs, prefer a DB-level query like repo.findByIsActiveTrue().
        return repo.findAll().stream().filter(c -> Boolean.TRUE.equals(c.getIsActive())).toList();
    }

    @GetMapping("/{slug}")
    public ResponseEntity<Course> bySlug(@PathVariable String slug) {
        // Return 200 with course if found, otherwise 404 Not Found
        return repo.findBySlugAndIsActiveTrue(slug)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}