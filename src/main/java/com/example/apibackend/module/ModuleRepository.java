package com.example.apibackend.module;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.List;

public interface ModuleRepository extends JpaRepository<Module, Long> {

    // List all modules for a course, ordered for UI display
    List<Module> findByCourseIdOrderByPositionAsc(Long courseId);

    // Prevent duplicates like two modules with same title in one course
    boolean existsByCourseIdAndTitleIgnoreCase(Long courseId, String title);

    // Quick counts for progress/admin dashboards
    long countByCourseId(Long courseId);

    // Rarely needed because of ON DELETE CASCADE, but handy for admin tools
    @Modifying
    @Query("delete from Module m where m.course.id = :courseId")
    void deleteByCourseId(Long courseId);
}