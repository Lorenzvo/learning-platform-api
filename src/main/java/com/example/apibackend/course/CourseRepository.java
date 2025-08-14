package com.example.apibackend.course;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * CRUD and query methods for Course entities.
 * Spring Data generates the implementation at runtime.
 */

public interface CourseRepository extends JpaRepository<Course, Long> {

    // Derived query method: SELECT * FROM courses WHERE slug=? AND is_active=1 LIMIT 1
    Optional<Course> findBySlugAndIsActiveTrue(String slug);

    /**
     * Custom search for courses with optional filters and pagination.
     * Uses JPQL to support case-insensitive search on title/description and exact match on level.
     * Only returns published (active) courses if published=true.
     */
    @org.springframework.data.jpa.repository.Query("""
        SELECT c FROM Course c
        WHERE (:q IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(c.description) LIKE LOWER(CONCAT('%', :q, '%')))
          AND (:level IS NULL OR c.level = :level)
          AND (:published IS NULL OR c.isActive = :published)
    """)
    org.springframework.data.domain.Page<Course> search(
        @org.springframework.data.repository.query.Param("q") String q,
        @org.springframework.data.repository.query.Param("level") String level,
        @org.springframework.data.repository.query.Param("published") Boolean published,
        org.springframework.data.domain.Pageable pageable
    );

    boolean existsBySlug(@NotBlank @Size(max = 255) String slug);
}