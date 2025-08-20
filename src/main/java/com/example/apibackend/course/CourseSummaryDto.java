package com.example.apibackend.course;

import com.example.apibackend.instructor.InstructorController.InstructorSummaryDto;

/**
 * DTO for paged course summaries in API responses.
 * Only exposes safe, necessary fields for the client.
 */
public record CourseSummaryDto(
    Long id,
    String title,
    String slug,
    String shortDesc,
    Integer priceCents,
    String level,
    Boolean published,
    double averageRating,
    InstructorSummaryDto instructor
) {}