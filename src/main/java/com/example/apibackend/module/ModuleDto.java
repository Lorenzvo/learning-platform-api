package com.example.apibackend.module;

import com.example.apibackend.lesson.LessonDto;
import java.util.List;

public record ModuleDto(
        Long id,
        String title,
        Integer position,
        List<LessonDto> lessons
) {}