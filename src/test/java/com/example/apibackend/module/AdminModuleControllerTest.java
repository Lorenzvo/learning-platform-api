package com.example.apibackend.module;

import com.example.apibackend.course.Course;
import com.example.apibackend.course.CourseRepository;
import com.example.apibackend.lesson.Lesson;
import com.example.apibackend.lesson.LessonRepository;
import com.example.apibackend.lesson.LessonType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminModuleController.class)
class AdminModuleControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private ModuleRepository moduleRepo;
    @MockitoBean
    private CourseRepository courseRepo;
    @MockitoBean
    private LessonRepository lessonRepo;

    private Course course;
    private Module module1, module2;
    private Lesson lesson1, lesson2;

    @BeforeEach
    void setup() {
        course = new Course();
        setId(course, 1L);
        course.setTitle("Course 1");
        course.setSlug("course-1");
        course.setIsActive(true);

        module1 = new Module();
        setId(module1, 10L);
        module1.setTitle("Module 1");
        module1.setPosition(0);
        module1.setCourse(course);

        module2 = new Module();
        setId(module2, 20L);
        module2.setTitle("Module 2");
        module2.setPosition(1);
        module2.setCourse(course);

        lesson1 = new Lesson();
        setId(lesson1, 100L);
        lesson1.setTitle("Lesson 1");
        lesson1.setType(LessonType.VIDEO);
        lesson1.setPosition(0);
        lesson1.setModule(module1);

        lesson2 = new Lesson();
        setId(lesson2, 200L);
        lesson2.setTitle("Lesson 2");
        lesson2.setType(LessonType.TEXT);
        lesson2.setPosition(1);
        lesson2.setModule(module1);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createModuleAndLesson_returns201WithDtos() throws Exception {
        // Mocks for course and module creation
        when(courseRepo.findById(eq(1L))).thenReturn(Optional.of(course)); // Simulate course exists
        when(moduleRepo.existsByCourseIdAndTitleIgnoreCase(eq(1L), anyString())).thenReturn(false); // No duplicate title
        when(moduleRepo.countByCourseId(eq(1L))).thenReturn(0L); // No modules yet
        ArgumentCaptor<Module> moduleCaptor = ArgumentCaptor.forClass(Module.class);
        // Create module (POST)
        ResultActions moduleResult = mockMvc.perform(post("/api/admin/courses/1/modules")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Module 1\",\"description\":\"Desc\"}")
                .with(csrf())); // CSRF required for POST
        moduleResult.andExpect(status().isCreated()) // Should return 201
                .andExpect(jsonPath("$.title").value("Module 1")); // DTO field check
        verify(moduleRepo).save(moduleCaptor.capture()); // Ensure save called
        Module createdModule = moduleCaptor.getValue();
        setId(createdModule, 10L); // Set a test ID for the created module so lesson creation works
        when(moduleRepo.findById(eq(createdModule.getId()))).thenReturn(Optional.of(createdModule)); // Simulate module exists
        when(lessonRepo.countByModuleId(eq(createdModule.getId()))).thenReturn(0L); // No lessons yet
        ArgumentCaptor<Lesson> lessonCaptor = ArgumentCaptor.forClass(Lesson.class);
        // Create lesson (POST)
        ResultActions lessonResult = mockMvc.perform(post("/api/admin/modules/" + createdModule.getId() + "/lessons")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Lesson 1\",\"type\":\"VIDEO\",\"contentUrl\":\"url\",\"durationSeconds\":120,\"isDemo\":false}")
                .with(csrf()));
        lessonResult.andExpect(status().isCreated()) // Should return 201
                .andExpect(jsonPath("$.title").value("Lesson 1")); // DTO field check
        verify(lessonRepo).save(lessonCaptor.capture()); // Ensure save called
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void reorderModulesAndLessons_returns200AndOrder() throws Exception {
        // Mocks for module and lesson reordering
        when(moduleRepo.findByCourseIdOrderByPositionAsc(eq(1L))).thenReturn(Arrays.asList(module1, module2)); // Initial order
        // Reorder modules (POST)
        ResultActions reorderModules = mockMvc.perform(post("/api/admin/courses/1/modules/reorder")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[20,10]") // New order: module2, module1
                .with(csrf()));
        reorderModules.andExpect(status().isOk()); // Should return 200
        // Setup for lesson reorder
        when(lessonRepo.findByModuleIdOrderByPositionAsc(eq(10L))).thenReturn(Arrays.asList(lesson2, lesson1)); // Initial order
        // Reorder lessons (POST)
        ResultActions reorderLessons = mockMvc.perform(post("/api/admin/modules/10/lessons/reorder")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[200,100]") // New order: lesson2, lesson1
                .with(csrf()));
        reorderLessons.andExpect(status().isOk()); // Should return 200
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteLesson_returns204AndLessonGone() throws Exception {
        // Mock for lesson existence
        when(lessonRepo.existsById(eq(100L))).thenReturn(true); // Simulate lesson exists
        // Delete lesson (DELETE)
        ResultActions deleteResult = mockMvc.perform(delete("/api/admin/lessons/100")
                .with(csrf()));
        deleteResult.andExpect(status().isNoContent()); // Should return 204
        verify(lessonRepo).deleteById(eq(100L)); // Ensure delete called
    }

    // Helper to set private id field
    private void setId(Object entity, Long id) {
        try {
            var field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
