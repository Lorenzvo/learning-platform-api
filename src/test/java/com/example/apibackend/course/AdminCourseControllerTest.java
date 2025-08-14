package com.example.apibackend.course;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc

class AdminCourseControllerTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    CourseRepository courseRepo;

    @BeforeEach
    void setup() {
        courseRepo.deleteAll();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCourse_returns201() throws Exception {
        String body = "{" +
                "\"title\":\"Test Course\"," +
                "\"slug\":\"test-course\"," +
                "\"price\":1000," +
                "\"currency\":\"USD\"," +
                "\"level\":\"beginner\"," +
                "\"shortDesc\":\"Short desc\"," +
                "\"longDesc\":\"Long description\"," +
                "\"thumbnailUrl\":\"http://example.com/image.png\"," +
                "\"published\":true" +
                "}";
        mockMvc.perform(post("/api/admin/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCourse_duplicateSlug_returns409() throws Exception {
        String body = "{" +
                "\"title\":\"Test Course\"," +
                "\"slug\":\"test-course\"," +
                "\"price\":1000," +
                "\"currency\":\"USD\"," +
                "\"level\":\"beginner\"," +
                "\"shortDesc\":\"Short desc\"," +
                "\"longDesc\":\"Long description\"," +
                "\"thumbnailUrl\":\"http://example.com/image.png\"," +
                "\"published\":true" +
                "}";
        // First create
        mockMvc.perform(post("/api/admin/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated());
        // Second create with same slug
        mockMvc.perform(post("/api/admin/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isConflict());
    }
}

