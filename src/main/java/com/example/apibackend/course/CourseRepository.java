package com.example.apibackend.course;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * CRUD and query methods for Course entities.
 * Spring Data generates the implementation at runtime.
 */

public interface CourseRepository extends JpaRepository<Course, Long> {

    // Derived query method: SELECT * FROM courses WHERE slug=? AND is_active=1 LIMIT 1
    Optional<Course> findBySlugAndIsActiveTrue(String slug);
}