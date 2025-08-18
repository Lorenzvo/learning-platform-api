package com.example.apibackend.review;

import com.example.apibackend.enrollment.Enrollment;
import com.example.apibackend.enrollment.EnrollmentRepository;
import com.example.apibackend.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReviewController.class)
class ReviewControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private ReviewRepository reviewRepo;
    @MockitoBean
    private EnrollmentRepository enrollmentRepo;

    private User user;
    private Enrollment enrollment;
    private Review review;

    @BeforeEach
    void setup() {
        user = new User();
        setId(user, 1L);
        user.setEmail("user@example.com");
        enrollment = new Enrollment();
        setId(enrollment, 2L);
        enrollment.setUser(user);
        enrollment.setStatus(Enrollment.EnrollmentStatus.ACTIVE);
        review = new Review();
        setId(review, 3L);
        review.setUser(user);
        review.setRating(5);
        review.setComment("Great course!");
        review.setCreatedAt(Instant.now().minusSeconds(600));
    }

    @Test
    @WithMockUser
    void getReviews_returnsPaged() throws Exception {
        when(reviewRepo.findByCourseId(eq(10L), any())).thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(review)));
        mockMvc.perform(get("/api/courses/10/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(3L))
                .andExpect(jsonPath("$.content[0].comment").value("Great course!"));
    }

    @Test
    @WithMockUser
    void postReview_enrolledUser_succeeds() throws Exception {
        when(enrollmentRepo.findByUserIdAndCourseId(eq(1L), eq(10L))).thenReturn(Optional.of(enrollment));
        when(reviewRepo.findByCourseId(eq(10L))).thenReturn(List.of(review));
        String body = "{\"rating\":5,\"comment\":\"This is a great course!\"}";
        ResultActions result = mockMvc.perform(post("/api/courses/10/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .with(csrf())
                .principal(() -> "user@example.com"));
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.comment").value("This is a great course!"));
    }

    @Test
    @WithMockUser
    void postReview_notEnrolled_returns403() throws Exception {
        when(enrollmentRepo.findByUserIdAndCourseId(eq(1L), eq(10L))).thenReturn(Optional.empty());
        String body = "{\"rating\":5,\"comment\":\"This is a great course!\"}";
        mockMvc.perform(post("/api/courses/10/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .with(csrf())
                .principal(() -> "user@example.com"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void postReview_tooShort_returns400() throws Exception {
        when(enrollmentRepo.findByUserIdAndCourseId(eq(1L), eq(10L))).thenReturn(Optional.of(enrollment));
        String body = "{\"rating\":5,\"comment\":\"Short\"}";
        mockMvc.perform(post("/api/courses/10/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .with(csrf())
                .principal(() -> "user@example.com"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void postReview_cooldown_returns429() throws Exception {
        when(enrollmentRepo.findByUserIdAndCourseId(eq(1L), eq(10L))).thenReturn(Optional.of(enrollment));
        Review recentReview = new Review();
        setId(recentReview, 4L);
        recentReview.setUser(user);
        recentReview.setRating(4);
        recentReview.setComment("Nice!");
        recentReview.setCreatedAt(Instant.now());
        when(reviewRepo.findByCourseId(eq(10L))).thenReturn(List.of(review, recentReview));
        String body = "{\"rating\":4,\"comment\":\"Another review!\"}";
        mockMvc.perform(post("/api/courses/10/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .with(csrf())
                .principal(() -> "user@example.com"))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteReview_admin_succeeds() throws Exception {
        review.setCourse(new com.example.apibackend.course.Course());
        setId(review.getCourse(), 10L);
        when(reviewRepo.findById(eq(3L))).thenReturn(Optional.of(review));
        mockMvc.perform(delete("/api/courses/10/reviews/3")
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteReview_wrongCourse_returns404() throws Exception {
        review.setCourse(new com.example.apibackend.course.Course());
        setId(review.getCourse(), 99L);
        when(reviewRepo.findById(eq(3L))).thenReturn(Optional.of(review));
        mockMvc.perform(delete("/api/courses/10/reviews/3")
                .with(csrf()))
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

