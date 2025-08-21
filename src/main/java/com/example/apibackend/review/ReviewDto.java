package com.example.apibackend.review;

import java.time.Instant;

public record ReviewDto(
    Long id,
    Long userId,
    String userEmail,
    int rating,
    String comment,
    Instant createdAt
) {
    public ReviewDto(Review review) {
        this(
            review.getId(),
            review.getUser() != null ? review.getUser().getId() : null,
            review.getUser() != null ? review.getUser().getEmail() : null,
            review.getRating(),
            review.getComment(),
            review.getCreatedAt()
        );
    }
    public static ReviewDto fromEntity(Review review) {
        return new ReviewDto(review);
    }
}
