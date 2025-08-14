package com.example.apibackend.module;


import com.example.apibackend.course.Course;
import com.example.apibackend.lesson.Lesson;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity // Marks this class as a JPA entity mapped to the "modules" table
@Table(
        name = "modules",
        indexes = {
                // Helps fast lookups per course & ordered fetches
                @Index(name = "idx_modules_course_position", columnList = "course_id, position")
        }
)
public class Module {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many modules belong to one course; LAZY avoids pulling course data unless needed
    @Setter
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false) // FK to courses.id
    private Course course;

    @Setter
    @Column(nullable = false, length = 200) // Short name shown in UI
    private String title;

    @Setter
    @Column(nullable = false) // Order within the course (0,1,2…)
    private Integer position;

    @Setter
    @Column(columnDefinition = "TEXT") // Optional longer blurb
    private String description;

    // One module has many lessons; cascade saves/deletes lessons with the module
    // orphanRemoval = true removes lessons if they're removed from the list
    @Setter
    @OneToMany(mappedBy = "module", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC") // Stable order; switch to "position ASC" if you add a lesson.position
    private List<Lesson> lessons = new ArrayList<>();

    // --- boilerplate ---
    public Module() {}
    public Module(Course course, String title, Integer position, String description) {
        this.course = course; this.title = title; this.position = position; this.description = description;
    }
}