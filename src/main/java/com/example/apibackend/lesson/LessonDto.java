package com.example.apibackend.lesson;

public record LessonDto(
        Long id,
        String title,
        String type,
        Integer durationSeconds,
        boolean isDemo
) {}
