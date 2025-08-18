package com.example.apibackend.instructor;

import com.example.apibackend.course.Course;
import com.example.apibackend.course.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InstructorController.class)
class InstructorControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private InstructorRepository instructorRepo;
    @MockitoBean
    private CourseRepository courseRepo;

    private Instructor instructor;
    private Course course;

    @BeforeEach
    void setup() {
        instructor = new Instructor();
        setId(instructor, 1L);
        instructor.setBio("Expert in Java");
        instructor.setAvatarUrl("http://avatar.com/1.png");
        instructor.setUser(new com.example.apibackend.user.User());
        instructor.getUser().setEmail("instructor@example.com");
        course = new Course();
        setId(course, 2L);
        course.setTitle("Java 101");
        course.setSlug("java-101");
        course.setIsActive(true);
    }

    @Test
    void getAllInstructors_returnsList() throws Exception {
        when(instructorRepo.findAll()).thenReturn(List.of(instructor));
        mockMvc.perform(get("/api/instructors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].bio").value("Expert in Java"));
    }

    @Test
    void getInstructorById_returnsDetail() throws Exception {
        when(instructorRepo.findById(anyLong())).thenReturn(Optional.of(instructor));
        when(courseRepo.findByInstructorId(anyLong())).thenReturn(List.of(course));
        mockMvc.perform(get("/api/instructors/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.bio").value("Expert in Java"))
                .andExpect(jsonPath("$.courses[0].title").value("Java 101"));
    }

    @Test
    void getInstructorById_notFound_returns404() throws Exception {
        when(instructorRepo.findById(anyLong())).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/instructors/99"))
                .andExpect(status().isNotFound());
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

