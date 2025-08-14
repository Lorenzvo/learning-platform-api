package com.example.apibackend.enrollment;

import com.example.apibackend.course.Course;
import com.example.apibackend.course.CourseRepository;
import com.example.apibackend.user.User;
import com.example.apibackend.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.ForkJoinPool;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "app.devEndpoints.enabled=true")
@AutoConfigureMockMvc
@ActiveProfiles("dev")

class DevEnrollmentControllerTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    UserRepository userRepo;
    @Autowired
    CourseRepository courseRepo;
    @Autowired
    EnrollmentRepository enrollmentRepo;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {
        enrollmentRepo.deleteAll();
        userRepo.deleteAll();
        courseRepo.deleteAll();

        jdbcTemplate.execute("ALTER TABLE users AUTO_INCREMENT = 1");
        jdbcTemplate.execute("ALTER TABLE enrollments AUTO_INCREMENT = 1");
        jdbcTemplate.execute("ALTER TABLE courses AUTO_INCREMENT = 1");

        User user = new User();
        user.setEmail("Test@gmail.com");
        user.setPasswordHash("password");
        user.setRole("USER");
        userRepo.save(user);
        Course course = new Course();
        course.setSlug("test-course");
        course.setTitle("Test Course");
        course.setPriceCents(1000);
        course.setIsActive(true);
        courseRepo.save(course);
    }

    @Test
    void createEnrollment_success() throws Exception {
        mockMvc.perform(post("/api/dev/enrollments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":1,\"courseId\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.courseId").value(1))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void createEnrollment_conflict() throws Exception {
        // First request creates enrollment
        mockMvc.perform(post("/api/dev/enrollments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":1,\"courseId\":1}"))
                .andExpect(status().isCreated());
        // Second request should return 409
        mockMvc.perform(post("/api/dev/enrollments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":1,\"courseId\":1}"))
                .andExpect(status().isConflict());
    }

//    @Test
//    void createEnrollment_disabled_returns404() throws Exception {
//        mockMvc.perform(post("/api/dev/enrollments")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content("{\"userId\":1,\"courseId\":1}"))
//                .andExpect(status().isNotFound());
//    }
}
