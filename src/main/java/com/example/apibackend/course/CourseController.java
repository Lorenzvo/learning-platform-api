package com.example.apibackend.course;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Public course catalog endpoints.
 * - GET /api/courses: list active courses
 * - GET /api/courses/{slug}: fetch an active course by slug
 */

@RestController
@RequestMapping("/api/courses")
@Validated // Enables bean validation on query params
public class CourseController {

    private final CourseRepository repo;

    // Constructor injection (preferred): Spring provides the repo at runtime
    public CourseController(CourseRepository repo) { this.repo = repo; }

    @GetMapping("/{slug}")
    public ResponseEntity<Course> bySlug(@PathVariable String slug) {
        // Return 200 with course if found, otherwise 404 Not Found
        return repo.findBySlugAndIsActiveTrue(slug)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/courses with pagination, sorting, and optional filters.
     * Handles any combination of query params (page, size, sort, q, level, published).
     * Returns a page of CourseSummaryDto, not JPA entities, for API safety and decoupling.
     * If no pagination/filter params are provided, returns all active courses as a single page.
     *
     * Refactored: Removed strict param matching so partial params work (Spring will always call this method for /api/courses).
     */
    @GetMapping
    public Page<CourseSummaryDto> searchCourses(
            @PageableDefault(page = 0, size = 12)
            @SortDefault.SortDefaults({@SortDefault(sort = "title")}) Pageable pageable,
            @RequestParam(required = false) @Size(max = 100) String q,
            @RequestParam(required = false) @Size(max = 20) String level,
            @RequestParam(required = false) Boolean published
    ) {
        // If any filter or pagination param is present, use repository search
        if (q != null || level != null || published != null || pageable.getPageSize() != 12 || pageable.getPageNumber() != 0) {
            Page<Course> page = repo.search(q, level, published, pageable);
            // Map entities to DTOs for API safety (never expose JPA entities directly)
            return page.map(c -> new CourseSummaryDto(
                    c.getId(),
                    c.getTitle(),
                    c.getSlug(),
                    c.getDescription() != null ? c.getDescription().substring(0, Math.min(80, c.getDescription().length())) : null,
                    c.getPriceCents(),
                    c.getLevel(),
                    c.getIsActive()
            ));
        } else {
            // No filters/pagination: return all active courses as a single page
            List<CourseSummaryDto> dtos = repo.findAll().stream()
                    .filter(c -> Boolean.TRUE.equals(c.getIsActive()))
                    .map(c -> new CourseSummaryDto(
                            c.getId(),
                            c.getTitle(),
                            c.getSlug(),
                            c.getDescription() != null ? c.getDescription().substring(0, Math.min(80, c.getDescription().length())) : null,
                            c.getPriceCents(),
                            c.getLevel(),
                            c.getIsActive()
                    )).toList();
            // Wrap in a single-page Page object
            return new org.springframework.data.domain.PageImpl<>(dtos);
        }
    }
}