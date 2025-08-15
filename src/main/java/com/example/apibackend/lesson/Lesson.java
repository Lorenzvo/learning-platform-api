package com.example.apibackend.lesson;

import jakarta.persistence.*;
import com.example.apibackend.module.Module;
import lombok.Getter;
import lombok.Setter;

@Getter
@Entity // Maps to "lessons" table
@Table(
        name = "lessons",
        indexes = {
                @Index(name = "idx_lessons_module", columnList = "module_id") // Fast fetch per module
        }
)
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Each lesson belongs to one module; LAZY to avoid unnecessary joins
    @Setter
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false) // FK to modules.id
    private Module module;

    @Setter
    @Column(nullable = false, length = 200) // Lesson display title
    private String title;

    // Store enum as text (easier to read/migrate than ORDINAL)
    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private LessonType type; // VIDEO or TEXT

    // For VIDEO, a media URL (S3/CDN); for TEXT, could be a doc URL or null
    @Setter
    @Column(name = "content_url", length = 512)
    private String contentUrl;

    // In seconds; nullable if unknown/not applicable
    @Setter
    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    // Demo/preview flag for marketing/free trial
    @Setter
    @Column(name = "is_demo", nullable = false)
    private boolean isDemo;

    // New: position for ordering lessons within a module
    @Setter
    @Column(nullable = false)
    private int position;

    // --- boilerplate ---
    public Lesson() {}
    public Lesson(Module module, String title, LessonType type, String contentUrl,
                  Integer durationSeconds, boolean demo, int position) {
        this.module = module; this.title = title; this.type = type;
        this.contentUrl = contentUrl; this.durationSeconds = durationSeconds; this.isDemo = demo; this.position = position;
    }
}
