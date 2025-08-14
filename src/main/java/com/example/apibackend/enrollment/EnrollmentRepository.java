package com.example.apibackend.enrollment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * CRUD and lightweight queries for Enrollment.
 */
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    // Fast existence check to block duplicate enrollments
    boolean existsByUserIdAndCourseId(Long userId, Long courseId);

    Optional<Enrollment> findByUserIdAndCourseId(Long userId, Long courseId);
}