package com.example.apibackend.course;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * JPA entity mapping to the `course` table.
 * Matches Flyway V1__init.sql exactly (column names and types).
 */

@Getter
@Entity
@Table(name = "courses")
public class Course {

    // Getters/Setters
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Uses MySQL AUTO_INCREMENT
    private Long id;

    @Setter
    @Column(nullable = false, unique = true, length = 255)
    private String slug; // Safe, unique URL identifier i.e java-basics

    @Setter
    @Column(nullable = false, length = 255)
    private String title;

    @Setter
    @Column(columnDefinition = "TEXT")
    private String description; // Longer text

    @Setter
    @Column(name = "price_cents", nullable = false)
    private Integer priceCents; // Store money in cents

    @Setter
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true; // Soft enable/disable flag

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false,
            columnDefinition = "timestamp default current_timestamp"
    )
    private Instant createdAt;

    @Column(
            name = "updated_at",
            nullable = false,
            columnDefinition = "timestamp default current_timestamp on update current_timestamp"
    )
    private Instant updatedAt;

}