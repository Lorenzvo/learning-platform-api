package com.example.apibackend.payment;

import com.example.apibackend.config.SecurityConfig;
import com.example.apibackend.course.Course;
import com.example.apibackend.course.CourseRepository;
import com.example.apibackend.user.User;
import com.example.apibackend.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)

class PaymentControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private CourseRepository courseRepository;

    private User testUser;
    private Course testCourse;
    private CheckoutResponseDTO testDto;

    @BeforeEach
    void setup() {
        testUser = new User();
        setId(testUser, 1L);
        testUser.setEmail("test@gmail.com");
        testUser.setPasswordHash("hashed-password");
        testUser.setRole("USER");
        // Set user ID for JWT principal simulation
        Mockito.when(userRepository.findById(anyLong())).thenReturn(java.util.Optional.of(testUser));
        testCourse = new Course();
        setId(testCourse, 1L);
        testCourse.setPriceCents(1000);
        testCourse.setCurrency("USD");
        Mockito.when(courseRepository.findById(anyLong())).thenReturn(java.util.Optional.of(testCourse));
        testDto = new CheckoutResponseDTO(42L, "cs_test_abc", 1000, "USD", "PENDING");
    }

    @Test
    @WithMockUser(username = "1", roles = {"USER"})
    void testCreateIntentReturnsPendingDto() throws Exception {
        // Simulate service returning a new intent
        when(paymentService.createOrGetPendingPayment(anyLong(), anyLong())).thenReturn(testDto);
        ResultActions result = mockMvc.perform(post("/api/checkout")
                .param("courseId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()));
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(42L))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser(username = "1", roles = {"USER"})
    void testIdempotentIntentReturnsSameDto() throws Exception {
        // Simulate idempotent service returning same intent
        when(paymentService.createOrGetPendingPayment(anyLong(), anyLong())).thenReturn(testDto);
        ResultActions first = mockMvc.perform(post("/api/checkout")
                .param("courseId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()));
        first.andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(42L))
                .andExpect(jsonPath("$.status").value("PENDING"));
        // Re-POST quickly, should return same DTO
        ResultActions second = mockMvc.perform(post("/api/checkout")
                .param("courseId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()));
        second.andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(42L))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    // Use reflection to set the private ID field on User or Course objects,
    // since they do not have public setters for ID for security reasons.
    private void setId(Object entity, Long id) {
        try {
            Field idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
