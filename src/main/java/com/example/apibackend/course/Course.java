package com.example.apibackend.course;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
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
    @Column(name = "short_description", length = 280)
    private String shortDescription;

    @Setter
    @Column(columnDefinition = "TEXT")
    private String description; // Longer text

    @Setter
    @Column(name = "price_cents", nullable = false)
    private Integer priceCents; // Store money in cents

    @Setter
    @Pattern(regexp = "^[A-Z]{3}$")
    @Column(nullable = false, length = 3, columnDefinition = "char(3) default 'USD'")
    private String currency; // ISO 4217 currency code, default to USD

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

    /**
     * Course difficulty/target audience (e.g., BEGINNER, INTERMEDIATE, ADVANCED).
     * Added in V3 migration. Supports API filtering and display.
     */

    @Setter
    @Column(name = "level")
    private String level;

    @Setter
    @Column(name = "thumbnail_url", length = 255)
    private String thumbnailUrl;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id")
    private com.example.apibackend.instructor.Instructor instructor;
    public com.example.apibackend.instructor.Instructor getInstructor() { return instructor; }

    // Fills shortDescription with long description if not set, compatible with legacy code as description was implemented first
    // Converts currency to uppercase before saving
    @PrePersist @PreUpdate
    private void normalize() {
        if ((shortDescription == null || shortDescription.isBlank()) && description != null) {
            // naive teaser: strip newlines and hard cut at ~180 chars
            String plain = description.replaceAll("\\s+", " ").trim();
            shortDescription = plain.length() <= 180 ? plain : plain.substring(0, 177) + "...";
        }
        if (currency != null) currency = currency.toUpperCase();
    }
}
