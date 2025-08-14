package com.example.apibackend.course;

import com.example.apibackend.lesson.Lesson;
import com.example.apibackend.lesson.LessonRepository;
import com.example.apibackend.module.Module;
import com.example.apibackend.module.ModuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // Ensures rollback after each test
class CourseControllerDemoLessonTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ModuleRepository moduleRepo;
    @Autowired
    private LessonRepository lessonRepo;
    @Autowired
    private CourseRepository courseRepo;

    private Long courseId;
    private Long demoLessonId;
    private Long nonDemoLessonId;

    @BeforeEach
    void setUp() {
        // Clean up all test data before each test
        lessonRepo.deleteAll();
        moduleRepo.deleteAll();
        courseRepo.deleteAll();
        System.out.println("Lessons after delete: " + lessonRepo.count());
        System.out.println("Modules after delete: " + moduleRepo.count());
        System.out.println("Courses after delete: " + courseRepo.count());

        // Seed a course
        Course course = new Course();
        course.setTitle("Demo Course");
        course.setSlug("demo-course");
        course.setIsActive(true);
        course.setLevel("beginner");
        course.setPriceCents(1000);
        courseRepo.save(course);
        courseId = course.getId();
        System.out.println("Seeded courseId: " + courseId);

        // Seed a module
        Module module = new Module();
        module.setCourse(course); // Ensure module is linked to course
        module.setTitle("Demo Module");
        module.setPosition(1);
        moduleRepo.save(module);
        Long moduleId = module.getId();
        System.out.println("Seeded moduleId: " + moduleId + ", module.courseId: " + (module.getCourse() != null ? module.getCourse().getId() : null));

        // Seed a demo lesson
        Lesson demoLesson = new Lesson();
        demoLesson.setModule(module); // Ensure lesson is linked to module
        demoLesson.setTitle("Demo Lesson");
        demoLesson.setType(com.example.apibackend.lesson.LessonType.VIDEO);
        demoLesson.setDurationSeconds(60);
        demoLesson.setDemo(true);
        lessonRepo.save(demoLesson);
        demoLessonId = demoLesson.getId();

        // Seed a non-demo lesson
        Lesson nonDemoLesson = new Lesson();
        nonDemoLesson.setModule(module);
        nonDemoLesson.setTitle("Non-Demo Lesson");
        nonDemoLesson.setType(com.example.apibackend.lesson.LessonType.VIDEO);
        nonDemoLesson.setDurationSeconds(60);
        nonDemoLesson.setDemo(false);
        lessonRepo.save(nonDemoLesson);
        nonDemoLessonId = nonDemoLesson.getId();
    }

    @Test
    void returnsTokenForDemoLesson() throws Exception {
        mockMvc.perform(get("/api/courses/" + courseId + "/demo/" + demoLessonId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.expiresAt").exists());
    }

    @Test
    void returns403ForNonDemoLesson() throws Exception {
        mockMvc.perform(get("/api/courses/" + courseId + "/demo/" + nonDemoLessonId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void returns404ForLessonNotFound() throws Exception {
        mockMvc.perform(get("/api/courses/" + courseId + "/demo/999999")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void returns404IfLessonNotInCourse() throws Exception {
        // Create another course and module, and a lesson not in the original course
        Course otherCourse = new Course();
        otherCourse.setTitle("Other Course");
        otherCourse.setSlug("other-course");
        otherCourse.setIsActive(true);
        otherCourse.setLevel("beginner");
        otherCourse.setPriceCents(1000);
        courseRepo.save(otherCourse);

        Module otherModule = new Module();
        otherModule.setTitle("Other Module");
        otherModule.setPosition(1);
        moduleRepo.save(otherModule);

        Lesson otherLesson = new Lesson();
        otherLesson.setTitle("Other Demo Lesson");
        otherLesson.setType(com.example.apibackend.lesson.LessonType.VIDEO);
        otherLesson.setDurationSeconds(60);
        otherLesson.setDemo(true);
        lessonRepo.save(otherLesson);
        Long otherLessonId = otherLesson.getId();

        // Try to access with original courseId
        mockMvc.perform(get("/api/courses/" + courseId + "/demo/" + otherLessonId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
