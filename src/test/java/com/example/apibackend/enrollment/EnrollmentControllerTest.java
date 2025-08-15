package com.example.apibackend.enrollment;

import com.example.apibackend.auth.JwtUtil;
import com.example.apibackend.user.User;
import com.example.apibackend.user.UserRepository;
import com.example.apibackend.course.Course;
import com.example.apibackend.config.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


import java.lang.reflect.Field;
import java.util.List;
import java.util.Arrays;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({EnrollmentController.class, AdminEnrollmentController.class})
@Import(SecurityConfig.class)
class EnrollmentControllerTest {
    @MockitoBean
    private EnrollmentRepository enrollmentRepo;
    @MockitoBean
    private UserRepository userRepo;
    @MockitoBean
    private JwtUtil jwtUtil;
    @Autowired
    private MockMvc mockMvc;

    private User user1;
    private User user2;
    private User admin;
    private Enrollment enrollment1;
    private Enrollment enrollment2;
    private Enrollment enrollment3;

    private Course realCourse(Long id, String currency) {
        Course course = new Course();
        // Set required fields
        try {
            java.lang.reflect.Field idField = Course.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(course, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        course.setSlug("test-course-" + id);
        course.setTitle("Test Course " + id);
        course.setPriceCents(1000);
        course.setCurrency(currency);
        course.setIsActive(true);
        return course;
    }

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        // Remove manual MockMvc setup. Spring will inject the correct MockMvc instance.

        // Setup users
        user1 = new User();
        setId(user1, 1L);
        user1.setEmail("user1@example.com");
        user1.setPasswordHash("password1");
        user1.setRole("USER");

        user2 = new User();
        setId(user2, 2L);
        user2.setEmail("user2@example.com");
        user2.setPasswordHash("password2");
        user2.setRole("USER");

        admin = new User();
        setId(admin, 3L);
        admin.setEmail("admin@example.com");
        admin.setPasswordHash("adminpassword");
        admin.setRole("ADMIN");

        // Setup enrollments, associating them with users and real courses
        enrollment1 = mock(Enrollment.class);
        when(enrollment1.getId()).thenReturn(101L);
        when(enrollment1.getUser()).thenReturn(user1);
        when(enrollment1.getCourse()).thenReturn(realCourse(201L, "USD"));
        when(enrollment1.getStatus()).thenReturn(Enrollment.EnrollmentStatus.ACTIVE);

        enrollment2 = mock(Enrollment.class);
        when(enrollment2.getId()).thenReturn(102L);
        when(enrollment2.getUser()).thenReturn(user1);
        when(enrollment2.getCourse()).thenReturn(realCourse(202L, "USD"));
        when(enrollment2.getStatus()).thenReturn(Enrollment.EnrollmentStatus.ACTIVE);

        enrollment3 = mock(Enrollment.class);
        when(enrollment3.getId()).thenReturn(103L);
        when(enrollment3.getUser()).thenReturn(user2);
        when(enrollment3.getCourse()).thenReturn(realCourse(203L, "USD"));
        when(enrollment3.getStatus()).thenReturn(Enrollment.EnrollmentStatus.ACTIVE);
    }

    /**
     * Tests that GET /api/enrollments/me returns only the current user's enrollments.
     * - Sets up mock enrollments for user1.
     * - Authenticates as user1.
     * - Expects only user1's enrollments in the response.
     */
    @Test
    void getMyEnrollments_returnsOnlyCurrentUserEnrollments() throws Exception {
        // Only user1's enrollments
        when(enrollmentRepo.findByUserId(anyLong())).thenReturn(Arrays.asList(enrollment1, enrollment2));
        Authentication auth = new UsernamePasswordAuthenticationToken(user1, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Debug: check that enrollments are returned from the mock
        List<Enrollment> debugEnrollments = enrollmentRepo.findByUserId(user1.getId());
        assert debugEnrollments.size() == 2 : "Mocked enrollments not returned as expected";

        mockMvc.perform(get("/api/enrollments/me")
                .principal(auth)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Should succeed
                .andExpect(jsonPath("$[0].id").value(101L)) // First enrollment's ID
                .andExpect(jsonPath("$[1].id").value(102L)) // Second enrollment's ID
                .andExpect(jsonPath("$[0].courseId").value(201L)) // First enrollment's course
                .andExpect(jsonPath("$[1].courseId").value(202L)); // Second enrollment's course
    }

    /**
     * Tests that GET /api/admin/enrollments returns all enrollments for admin.
     * - Sets up mock enrollments for two users.
     * - Authenticates as admin.
     * - Expects all enrollments in the response, including userId, courseId, id, and status.
     */
    @Test
    void getAllEnrollments_asAdmin_returnsGlobalList() throws Exception {
        when(enrollmentRepo.findAll()).thenReturn(Arrays.asList(enrollment1, enrollment2, enrollment3));
        Authentication auth = new UsernamePasswordAuthenticationToken(admin, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        mockMvc.perform(get("/api/admin/enrollments")
                .principal(auth)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Should succeed for admin
                .andExpect(jsonPath("$[0].id").value(101L)) // Enrollment1
                .andExpect(jsonPath("$[0].userId").value(user1.getId()))
                .andExpect(jsonPath("$[0].courseId").value(201L))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$[1].id").value(102L)) // Enrollment2
                .andExpect(jsonPath("$[1].userId").value(user1.getId()))
                .andExpect(jsonPath("$[1].courseId").value(202L))
                .andExpect(jsonPath("$[1].status").value("ACTIVE"))
                .andExpect(jsonPath("$[2].id").value(103L)) // Enrollment3
                .andExpect(jsonPath("$[2].userId").value(user2.getId()))
                .andExpect(jsonPath("$[2].courseId").value(203L))
                .andExpect(jsonPath("$[2].status").value("ACTIVE"));
    }

    /**
     * Tests that non-admin users cannot access the admin enrollments endpoint.
     * - Authenticates as a regular user.
     * - Expects 403 Forbidden status.
     */
    @Test
    void getAllEnrollments_asNonAdmin_returnsForbidden() throws Exception {
        Authentication auth = new UsernamePasswordAuthenticationToken(user2, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        mockMvc.perform(get("/api/admin/enrollments")
                .principal(auth)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden()); // Should be forbidden for non-admin
    }

    // Use reflection to set the private ID field on User objects,
    // since User does not have a public setter for ID for security reasons.
    private void setId(User user, Long id) {
        try {
            Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
