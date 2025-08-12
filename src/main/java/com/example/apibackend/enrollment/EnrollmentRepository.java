package com.example.apibackend.enrollment;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * CRUD and lightweight queries for Enrollment.
 */
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    // Fast existence check to block duplicate enrollments
    boolean existsByUserIdAndCourseId(Long userId, Long courseId);
}