package com.example.apibackend.enrollment;

import com.example.apibackend.user.User;
import com.example.apibackend.course.Course;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Enrollment entity linking a User and a Course.
 * Matches the unique constraint (user_id, course_id) from V1__init.sql.
 */

@Getter
@Entity
@Table(
        name = "enrollments",
        uniqueConstraints = @UniqueConstraint(name="uq_enroll", columnNames={"user_id","course_id"})
)
public class Enrollment {
    public static enum EnrollmentStatus { PENDING, ACTIVE, CANCELED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    // Many enrollments belong to one user
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_enroll_user")
    )
    private User user;

    @Setter
    // Many enrollments belong to one course
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "course_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_enroll_course")
    )
    private Course course;

    @Setter
    // Mirror the ENUM('PENDING','ACTIVE','CANCELED') from SQL
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "enum('PENDING','ACTIVE','CANCELED')")
    private EnrollmentStatus status = EnrollmentStatus.PENDING;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false,
            columnDefinition = "timestamp default current_timestamp"
    )
    private Instant createdAt;

    @Setter
    @Column(name = "revoked_at")
    private Instant revokedAt;

}