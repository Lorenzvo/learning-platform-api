package com.example.apibackend.course;

import java.util.List;
import com.example.apibackend.module.ModuleDto;

public record CourseDetailDto(
        String title,
        Integer price,
        String slug,
        String level,
        String longDesc,
        String thumbnailUrl,
        boolean published,
        List<ModuleDto> modules
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
            List.of() // No modules for admin create response
        );
    }
}
