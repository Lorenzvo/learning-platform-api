package com.example.apibackend.course;

import com.example.apibackend.enrollment.Enrollment;
import com.example.apibackend.enrollment.EnrollmentRepository;
import com.example.apibackend.payment.Payment;
import com.example.apibackend.payment.PaymentRepository;
import com.example.apibackend.user.User;
import com.example.apibackend.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminMetricsSummaryTest {
    @Autowired MockMvc mockMvc;
    @Autowired CourseRepository courseRepo;
    @Autowired EnrollmentRepository enrollmentRepo;
    @Autowired PaymentRepository paymentRepo;
    @Autowired UserRepository userRepo;

    @BeforeEach
    void setup() {
        paymentRepo.deleteAll();
        enrollmentRepo.deleteAll();
        courseRepo.deleteAll();
        userRepo.deleteAll();
        // Create one user
        User user = new User();
        user.setPasswordHash("password");
        user.setEmail("test@example.com");
        user.setRole("USER");
        userRepo.save(user);
        // Create one course
        Course course = new Course();
        course.setTitle("Test Course");
        course.setSlug("test-course");
        course.setPriceCents(1000);
        course.setCurrency("USD");
        course.setLevel("beginner");
        course.setShortDescription("Short desc");
        course.setDescription("Long desc");
        course.setThumbnailUrl("http://example.com/img.png");
        course.setIsActive(true);
        courseRepo.save(course);
        // Create one enrollment
        Enrollment enrollment = new Enrollment();
        enrollment.setUser(user);
        enrollment.setCourse(course);
        enrollment.setStatus(Enrollment.EnrollmentStatus.ACTIVE);
        enrollmentRepo.save(enrollment);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void metricsSummary_noPayments() throws Exception {
        mockMvc.perform(get("/api/admin/courses/metrics/summary")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCourses").value(1))
                .andExpect(jsonPath("$.totalEnrollments").value(1))
                .andExpect(jsonPath("$.revenueUsd").value(0.0));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void metricsSummary_withPayment() throws Exception {
        // Insert a successful payment
        Payment payment = new Payment();
        payment.setUser(userRepo.findAll().getFirst());
        payment.setCourse(courseRepo.findAll().getFirst());
        payment.setAmountCents(12345);
        payment.setCurrency("USD");
        payment.setStatus(Payment.PaymentStatus.SUCCESS);
        paymentRepo.save(payment);
        mockMvc.perform(get("/api/admin/courses/metrics/summary")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCourses").value(1))
                .andExpect(jsonPath("$.totalEnrollments").value(1))
                .andExpect(jsonPath("$.revenueUsd").value(123.45));
    }

    @AfterEach
    void cleanup() {
        paymentRepo.deleteAll();
        enrollmentRepo.deleteAll();
        courseRepo.deleteAll();
        userRepo.deleteAll();
    }
}

