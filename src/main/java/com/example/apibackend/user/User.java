package com.example.apibackend.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * JPA entity mapping to the `users` table.
 * Matches Flyway V1__init.sql exactly (column names and types).
 */

// No setters for id and createdAt to prevent accidental changes

@Getter
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Uses MySQL AUTO_INCREMENT
    private Long id;

    @Setter
    @Column(nullable = false, unique = true, length = 255)
    private String email; // Unique login identifier

    @Setter
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash; // Store BCrypt hash, never plaintext

    @Setter
    @Column(nullable = false, length = 20)
    private String role; // e.g., "USER" or "ADMIN"

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false,
            columnDefinition = "timestamp default current_timestamp"
    )
    private Instant createdAt; // audit timestamp (set by DB default)

    @Setter
    @Column(name = "deleted_at")
    private Instant deletedAt; // audit timestamp for soft deletion

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

}
