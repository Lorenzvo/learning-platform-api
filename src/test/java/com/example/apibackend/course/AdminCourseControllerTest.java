package com.example.apibackend.course;

import com.example.apibackend.enrollment.EnrollmentRepository;
import com.example.apibackend.payment.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminCourseController.class)
class AdminCourseControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private CourseRepository courseRepo;
    @MockitoBean
    private EnrollmentRepository enrollmentRepo;
    @MockitoBean
    private PaymentRepository paymentRepo;

    private Course course;

    @BeforeEach
    void setup() {
        course = new Course();
        setId(course, 1L);
        course.setTitle("Test Course");
        course.setSlug("test-course");
        course.setPriceCents(1000);
        course.setCurrency("USD");
        course.setIsActive(false);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateCourse_withAdmin_returnsUpdatedDto() throws Exception {
        // Mock finding the course by ID and ensure no slug conflict
        when(courseRepo.findById(eq(1L))).thenReturn(Optional.of(course));
        when(courseRepo.existsBySlug(anyString())).thenReturn(false);
        // Prepare update payload with new title and slug
        String body = "{\"title\":\"Updated Title\",\"slug\":\"updated-slug\"}";
        // Perform PUT request as admin with CSRF token
        ResultActions result = mockMvc.perform(put("/api/admin/courses/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .with(csrf()));
        // Expect 200 OK and verify updated fields in response
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.slug").value("updated-slug"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void publishCourse_withAdmin_setsPublishedTrue() throws Exception {
        // Mock finding the course by ID
        when(courseRepo.findById(eq(1L))).thenReturn(Optional.of(course));
        // Perform POST request to publish endpoint as admin
        ResultActions result = mockMvc.perform(post("/api/admin/courses/1/publish")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()));
        // Expect 200 OK and published flag set to true in response
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.published").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateCourse_duplicateSlug_returns409() throws Exception {
        // Mock finding the course and simulate slug conflict
        when(courseRepo.findById(eq(1L))).thenReturn(Optional.of(course));
        when(courseRepo.existsBySlug(eq("duplicate-slug"))).thenReturn(true);
        // Prepare update payload with duplicate slug
        String body = "{\"slug\":\"duplicate-slug\"}";
        // Perform PUT request as admin with CSRF token
        ResultActions result = mockMvc.perform(put("/api/admin/courses/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .with(csrf()));
        // Expect 409 Conflict and error message about slug
        result.andExpect(status().isConflict())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Slug already exists")));
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
