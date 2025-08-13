// ...existing code...
package com.example.apibackend.course;

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
    Boolean published
) {}
// ...existing code...

