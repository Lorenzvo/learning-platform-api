package com.example.apibackend.cart;

import lombok.Data;

/**
 * DTO for frontend cart display
 */
@Data
public class CartItemDto {
    private Long courseId;
    private String title;
    private Integer priceCents;
    private String thumbnailUrl;
    // Add other course info if needed
    public CartItemDto(Long courseId, String title, Integer priceCents, String thumbnailUrl) {
        this.courseId = courseId;
        this.title = title;
        this.priceCents = priceCents;
        this.thumbnailUrl = thumbnailUrl;
    }
}

