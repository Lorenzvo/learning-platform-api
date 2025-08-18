package com.example.apibackend.course;

import java.util.List;
import com.example.apibackend.module.ModuleDto;
import com.example.apibackend.instructor.InstructorController.InstructorSummaryDto;

public record CourseDetailDto(
        String title,
        Integer price,
        String slug,
        String level,
        String longDesc,
        String thumbnailUrl,
        boolean published,
        List<ModuleDto> modules,
        InstructorSummaryDto instructor,
        double avgRating,
        long reviewCount
) {
    public CourseDetailDto(Course course) {
        this(
            course.getTitle(),
            course.getPriceCents(),
            course.getSlug(),
            course.getLevel(),
            course.getDescription(),
            course.getThumbnailUrl(),
            Boolean.TRUE.equals(course.getIsActive()),
            List.of(), // No modules for admin create response
            null,
            0.0,
            0L
        );
    }
}
