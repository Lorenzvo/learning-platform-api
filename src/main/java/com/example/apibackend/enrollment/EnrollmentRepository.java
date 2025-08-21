package com.example.apibackend.enrollment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * CRUD and lightweight queries for Enrollment.
 */
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    // Fast existence check to block duplicate enrollments
    boolean existsByUserIdAndCourseId(Long userId, Long courseId);

    Optional<Enrollment> findByUserIdAndCourseId(Long userId, Long courseId);

    List<Enrollment> findByUserId(Long userId);

    // Only return enrollments for users who are not soft-deleted
    List<Enrollment> findByUserIdAndUser_DeletedAtIsNull(Long userId);

    // Only return enrollments for users who are not soft-deleted, paginated
    org.springframework.data.domain.Page<Enrollment> findByUserIdAndUser_DeletedAtIsNull(Long userId, org.springframework.data.domain.Pageable pageable);

    long countByUserId(Long id);

    long countByUserIdAndUser_DeletedAtIsNull(Long userId);
}
