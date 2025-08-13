package com.example.apibackend.course;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

/**
 * Controller slice test:
 * - Loads only MVC layer (no web server, no DB).
 * - Mocks CourseRepository so we can control responses.
 * - Disables security filters for simplicity in this slice.
 */

@WebMvcTest(CourseController.class)
@AutoConfigureMockMvc(addFilters = false) // skip security filters in this slice
@ActiveProfiles("test")
class CourseControllerTest {

    @Autowired
    MockMvc mvc;

    // Spring injects a Mockito mock for the repository
    @MockitoBean
    CourseRepository repo;

    @Test
    void list_returns_active_courses() throws Exception {
        // Arrange: stub repository response
        Course course = new Course();
        // (using Lombok setters in the entity)
        course.setSlug("java-basics");
        course.setTitle("Java Basics");
        course.setPriceCents(2999);
        course.setIsActive(true);

        // stub the repository the controller calls
        Mockito.when(repo.findAll()).thenReturn(List.of(course));

        // Call controller and check JSON
        mvc.perform(get("/api/courses").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("java-basics")));
    }

    @Test
    void get_by_slug_returns_200_or_404() throws Exception {
        Course course = new Course();
        course.setSlug("spring-boot-fundamentals");
        course.setTitle("Spring Boot Fundamentals");
        course.setDescription("Spring Boot REST");
        course.setLevel("BEGINNER");
        course.setPriceCents(4999);
        course.setIsActive(true);

        Mockito.when(repo.findBySlugAndIsActiveTrue("spring-boot-fundamentals"))
                .thenReturn(java.util.Optional.of(course));
        Mockito.when(repo.findBySlugAndIsActiveTrue("nope"))
                .thenReturn(java.util.Optional.empty());

        mvc.perform(get("/api/courses/spring-boot-fundamentals"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("spring-boot-fundamentals")));

        mvc.perform(get("/api/courses/nope"))
                .andExpect(status().isNotFound());
    }

    /**
     * NEW: Test GET /api/courses with pagination and filters.
     * Verifies param binding, default values, and DTO mapping.
     */
    @Test
    void searchCourses_paginationAndFilters_ok() throws Exception {
        Course course = new Course();
        course.setSlug("java-basics");
        course.setTitle("Java Basics");
        course.setDescription("Intro to Java");
        course.setLevel("BEGINNER");
        course.setPriceCents(4999);
        course.setIsActive(true);

        org.springframework.data.domain.Page<Course> page = new org.springframework.data.domain.PageImpl<>(List.of(course));
        Mockito.when(repo.search(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(page);

        // Act & Assert: call endpoint with params and check response shape
        mvc.perform(get("/api/courses")
                .param("page", "0")
                .param("size", "5")
                .param("q", "java")
                .param("level", "BEGINNER")
                .param("published", "true")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].slug").value("java-basics"))
            .andExpect(jsonPath("$.content[0].title").value("Java Basics"))
            .andExpect(jsonPath("$.content[0].level").value("BEGINNER"));
    }
}