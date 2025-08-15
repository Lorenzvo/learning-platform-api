package com.example.apibackend.module;

import com.example.apibackend.course.CourseRepository;
import com.example.apibackend.lesson.Lesson;
import com.example.apibackend.lesson.LessonDto;
import com.example.apibackend.lesson.LessonRepository;
import com.example.apibackend.lesson.LessonType;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminModuleController {
    private final ModuleRepository moduleRepo;
    private final CourseRepository courseRepo;
    private final LessonRepository lessonRepo;

    public AdminModuleController(ModuleRepository moduleRepo, CourseRepository courseRepo, LessonRepository lessonRepo) {
        this.moduleRepo = moduleRepo;
        this.courseRepo = courseRepo;
        this.lessonRepo = lessonRepo;
    }

    // --- MODULE ENDPOINTS ---

    /**
     * Create a module for a course
     */
    @PostMapping("/courses/{courseId}/modules")
    public ResponseEntity<?> createModule(@PathVariable Long courseId, @Validated @RequestBody CreateModuleRequest req) {
        var courseOpt = courseRepo.findById(courseId);
        if (courseOpt.isEmpty()) return ResponseEntity.notFound().build();
        // Prevent duplicate titles in course
        if (moduleRepo.existsByCourseIdAndTitleIgnoreCase(courseId, req.title)) {
            return ResponseEntity.status(409).body("Module title already exists in this course");
        }
        // Position: append to end
        int position = (int) moduleRepo.countByCourseId(courseId);
        Module module = new Module(courseOpt.get(), req.title, position, req.description);
        moduleRepo.save(module);
        return ResponseEntity.status(201).body(toDto(module));
    }

    /**
     * Update a module
     */
    @PutMapping("/modules/{id}")
    public ResponseEntity<?> updateModule(@PathVariable Long id, @Validated @RequestBody UpdateModuleRequest req) {
        var moduleOpt = moduleRepo.findById(id);
        if (moduleOpt.isEmpty()) return ResponseEntity.notFound().build();
        Module module = moduleOpt.get();
        // Prevent cross-course moves by not allowing courseId changes
        if (req.title != null) module.setTitle(req.title);
        if (req.description != null) module.setDescription(req.description);
        moduleRepo.save(module);
        return ResponseEntity.ok(toDto(module));
    }

    /**
     * Delete a module (cascade deletes lessons)
     */
    @DeleteMapping("/modules/{id}")
    public ResponseEntity<?> deleteModule(@PathVariable Long id) {
        if (!moduleRepo.existsById(id)) return ResponseEntity.notFound().build();
        moduleRepo.deleteById(id);
        // Orphan prevention: lessons are cascade deleted
        return ResponseEntity.noContent().build();
    }

    /**
     * Reorder modules in a course
     */
    @PostMapping("/courses/{courseId}/modules/reorder")
    @Transactional
    public ResponseEntity<?> reorderModules(@PathVariable Long courseId, @RequestBody List<Long> orderedIds) {
        List<Module> modules = moduleRepo.findByCourseIdOrderByPositionAsc(courseId);
        if (modules.size() != orderedIds.size() || !new HashSet<>(orderedIds).equals(modules.stream().map(Module::getId).collect(Collectors.toSet()))) {
            return ResponseEntity.badRequest().body("Module IDs mismatch or missing");
        }
        // Use forEach with index to avoid non-final variable in lambda
        for (int idx = 0; idx < orderedIds.size(); idx++) {
            Long moduleId = orderedIds.get(idx);
            Module m = modules.stream().filter(mod -> mod.getId().equals(moduleId)).findFirst().get();
            m.setPosition(idx);
            moduleRepo.save(m);
        }
        return ResponseEntity.ok().build();
    }

    // --- LESSON ENDPOINTS ---

    /**
     * Create a lesson in a module
     */
    @PostMapping("/modules/{moduleId}/lessons")
    public ResponseEntity<?> createLesson(@PathVariable Long moduleId, @Validated @RequestBody CreateLessonRequest req) {
        var moduleOpt = moduleRepo.findById(moduleId);
        if (moduleOpt.isEmpty()) return ResponseEntity.notFound().build();
        int position = (int) lessonRepo.countByModuleId(moduleId);
        Lesson lesson = new Lesson(moduleOpt.get(), req.title, req.type, req.contentUrl, req.durationSeconds, req.isDemo, position);
        lessonRepo.save(lesson);
        return ResponseEntity.status(201).body(toDto(lesson));
    }

    /**
     * Update a lesson
     */
    @PutMapping("/lessons/{id}")
    public ResponseEntity<?> updateLesson(@PathVariable Long id, @Validated @RequestBody UpdateLessonRequest req) {
        var lessonOpt = lessonRepo.findById(id);
        if (lessonOpt.isEmpty()) return ResponseEntity.notFound().build();
        Lesson lesson = lessonOpt.get();
        // Prevent cross-module moves by not allowing moduleId changes
        if (req.title != null) lesson.setTitle(req.title);
        if (req.type != null) lesson.setType(req.type);
        if (req.contentUrl != null) lesson.setContentUrl(req.contentUrl);
        if (req.durationSeconds != null) lesson.setDurationSeconds(req.durationSeconds);
        if (req.isDemo != null) lesson.setDemo(req.isDemo);
        lessonRepo.save(lesson);
        return ResponseEntity.ok(toDto(lesson));
    }

    /**
     * Delete a lesson
     */
    @DeleteMapping("/lessons/{id}")
    public ResponseEntity<?> deleteLesson(@PathVariable Long id) {
        if (!lessonRepo.existsById(id)) return ResponseEntity.notFound().build();
        lessonRepo.deleteById(id);
        // Orphan prevention: lesson is deleted, no orphaned data
        return ResponseEntity.noContent().build();
    }

    /**
     * Reorder lessons in a module
     */
    @PostMapping("/modules/{moduleId}/lessons/reorder")
    @Transactional
    public ResponseEntity<?> reorderLessons(@PathVariable Long moduleId, @RequestBody List<Long> orderedIds) {
        List<Lesson> lessons = lessonRepo.findByModuleIdOrderByPositionAsc(moduleId);
        if (lessons.size() != orderedIds.size() || !new HashSet<>(orderedIds).equals(lessons.stream().map(Lesson::getId).collect(Collectors.toSet()))) {
            return ResponseEntity.badRequest().body("Lesson IDs mismatch or missing");
        }
        // Use forEach with index to avoid non-final variable in lambda
        for (int idx = 0; idx < orderedIds.size(); idx++) {
            Long lessonId = orderedIds.get(idx);
            Lesson l = lessons.stream().filter(les -> les.getId().equals(lessonId)).findFirst().get();
            l.setPosition(idx);
            lessonRepo.save(l);
        }
        return ResponseEntity.ok().build();
    }

    // --- DTOs for requests ---
    @Data
    public static class CreateModuleRequest {
        public String title;
        public String description;
    }
    @Data
    public static class UpdateModuleRequest {
        public String title;
        public String description;
    }
    @Data
    public static class CreateLessonRequest {
        public String title;
        public LessonType type;
        public String contentUrl;
        public Integer durationSeconds;
        public boolean isDemo;
    }
    @Data
    public static class UpdateLessonRequest {
        public String title;
        public LessonType type;
        public String contentUrl;
        public Integer durationSeconds;
        public Boolean isDemo;
    }

    // --- DTO conversion helpers ---
    private ModuleDto toDto(Module m) {
        List<LessonDto> lessons = m.getLessons().stream().map(this::toDto).collect(Collectors.toList());
        return new ModuleDto(m.getId(), m.getTitle(), m.getPosition(), lessons);
    }
    private LessonDto toDto(Lesson l) {
        return new LessonDto(l.getId(), l.getTitle(), l.getType().name(), l.getDurationSeconds(), l.isDemo());
    }

    // --- Comments ---
    // - Orphan prevention: deleting a module cascade deletes lessons; deleting a lesson is safe.
    // - Cross-entity move guards: do not allow changing courseId/moduleId on update to prevent data inconsistencies.
    // - Reorder endpoints update position fields to maintain UI order and avoid bugs with drag-and-drop.
    // - DTOs are consistent with existing ModuleDto/LessonDto for API compatibility.
}
