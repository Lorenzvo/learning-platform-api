package com.example.apibackend.lesson;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


public interface LessonRepository extends JpaRepository<Lesson, Long> {

    // Show lessons inside a module
    List<Lesson> findByModuleIdOrderByIdAsc(Long moduleId);
    // If adding lesson.position for UI, switch to OrderByPositionAsc

    // Show lessons inside a module, ordered by position for UI
    List<Lesson> findByModuleIdOrderByPositionAsc(Long moduleId);

    // Cross-entity property path: all lessons across a course (paged)
    Page<Lesson> findByModuleCourseId(Long courseId, Pageable pageable);

    // Find demo/preview lesson quickly
    Optional<Lesson> findFirstByModuleIdAndIsDemoTrueOrderByIdAsc(Long moduleId);

    // Useful stats
    long countByModuleId(Long moduleId);

    // Example of avoiding N+1 when you need the parent with the lesson
    @EntityGraph(attributePaths = "module")
    Optional<Lesson> findWithModuleById(Long id);
}