package com.example.apibackend.review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findByCourseId(Long courseId, Pageable pageable);
    List<Review> findByCourseId(Long courseId);
    long countByCourseId(Long courseId);
    
    // Returns average rating for a course (custom JPQL)
    @org.springframework.data.jpa.repository.Query("SELECT AVG(r.rating) FROM Review r WHERE r.course.id = :courseId")
    Double findAverageRatingByCourseId(@org.springframework.data.repository.query.Param("courseId") Long courseId);
}
