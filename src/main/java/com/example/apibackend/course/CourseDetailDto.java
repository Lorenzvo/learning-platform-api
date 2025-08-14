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
) {}