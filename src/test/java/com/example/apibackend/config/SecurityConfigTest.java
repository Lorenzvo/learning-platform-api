package com.example.apibackend.config;

import com.example.apibackend.course.Course;
import com.example.apibackend.course.CourseRepository;
import com.example.apibackend.lesson.Lesson;
import com.example.apibackend.lesson.LessonRepository;
import com.example.apibackend.module.Module;
import com.example.apibackend.module.ModuleRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(properties = "app.cors.allowed-origins=http://localhost:3000")
@SpringBootTest
@Transactional
@AutoConfigureMockMvc

class SecurityConfigTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    CourseRepository courseRepo;
    @Autowired
    ModuleRepository moduleRepo;
    @Autowired
    LessonRepository lessonRepo;
    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {
        lessonRepo.deleteAll();
        moduleRepo.deleteAll();
        courseRepo.deleteAll();

        jdbcTemplate.execute("ALTER TABLE lessons AUTO_INCREMENT = 1");
        jdbcTemplate.execute("ALTER TABLE modules AUTO_INCREMENT = 1");
        jdbcTemplate.execute("ALTER TABLE courses AUTO_INCREMENT = 1");
        // Seed a course
        Course course = new Course();
        course.setTitle("Demo Course");
        course.setSlug("demo-course");
        course.setIsActive(true);
        course.setLevel("beginner");
        course.setPriceCents(1000);
        course.setCurrency("USD");
        courseRepo.save(course);

        // Seed a module
        Module module = new Module();
        module.setCourse(course); // Ensure module is linked to course
        module.setTitle("Demo Module");
        module.setPosition(1);
        moduleRepo.save(module);

        // Seed a demo lesson
        Lesson demoLesson = new Lesson();
        demoLesson.setModule(module); // Ensure lesson is linked to module
        demoLesson.setTitle("Demo Lesson");
        demoLesson.setType(com.example.apibackend.lesson.LessonType.VIDEO);
        demoLesson.setDurationSeconds(60);
        demoLesson.setDemo(true);
        lessonRepo.save(demoLesson);

    }

    @Test
    void allUsersCanAccessCoursesAndDemoToken() throws Exception {
        mockMvc.perform(get("/api/courses").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/courses/1/demo/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void adminEndpointsRequireAuth() throws Exception {
        mockMvc.perform(get("/api/admin/courses").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized()); // or isForbidden() if using JWT
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanAccessAdminEndpoints() throws Exception {
        mockMvc.perform(get("/api/admin/courses").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @AfterEach
    void cleanup() {
        lessonRepo.deleteAll();
        moduleRepo.deleteAll();
        courseRepo.deleteAll();
    }
}
