package com.example.apibackend.payment;

import com.example.apibackend.course.Course;
import com.example.apibackend.email.EmailService;
import com.example.apibackend.enrollment.Enrollment;
import com.example.apibackend.enrollment.EnrollmentRepository;
import com.example.apibackend.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentWebhookController.class)
class PaymentWebhookControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentRepository paymentRepo;
    @MockitoBean
    private EnrollmentRepository enrollmentRepo;
    @MockitoBean
    private EmailService emailService;

    private Payment payment;
    private User user;
    private Course course;

    private final String validSignature = "stub-secret";

    @BeforeEach
    void setup() {
        user = new User();
        setId(user, 10L);
        course = new Course();
        setId(course, 20L);
        payment = new Payment();
        setId(payment, 100L);
        payment.setUser(user);
        payment.setCourse(course);
        payment.setStatus(Payment.PaymentStatus.PENDING);
        payment.setAmountCents(1000);
        payment.setCurrency("USD");
    }

    @Test
    @WithMockUser(username = "webhook", roles = {"ADMIN"}) // Webhook endpoints should be protected
    void webhookSuccessCreatesEnrollment() throws Exception {
        when(paymentRepo.findById(eq(100L))).thenReturn(Optional.of(payment));
        when(enrollmentRepo.existsByUserIdAndCourseId(eq(10L), eq(20L))).thenReturn(false);
        String body = "{\"paymentId\":100,\"gatewayTxnId\":\"gw123\",\"status\":\"SUCCESS\"}";
        ResultActions result = mockMvc.perform(post("/api/webhooks/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Signature", validSignature)
                .content(body)
                .with(csrf()));
        result.andExpect(status().isOk());
        // Payment should be marked SUCCESS
        assertThat(payment.getStatus()).isEqualTo(Payment.PaymentStatus.SUCCESS);
        assertThat(payment.getGatewayTxnId()).isEqualTo("gw123");
        // Enrollment should be created
        ArgumentCaptor<Enrollment> captor = ArgumentCaptor.forClass(Enrollment.class);
        verify(enrollmentRepo).save(captor.capture());
        Enrollment enrollment = captor.getValue();
        assertThat(enrollment.getUser().getId()).isEqualTo(10L);
        assertThat(enrollment.getCourse().getId()).isEqualTo(20L);
        assertThat(enrollment.getStatus()).isEqualTo(Enrollment.EnrollmentStatus.ACTIVE);
    }

    @Test
    @WithMockUser(username = "webhook", roles = {"ADMIN"})
    void webhookSuccessIsIdempotent() throws Exception {
        payment.setStatus(Payment.PaymentStatus.SUCCESS);
        when(paymentRepo.findById(eq(100L))).thenReturn(Optional.of(payment));
        String body = "{\"paymentId\":100,\"gatewayTxnId\":\"gw123\",\"status\":\"SUCCESS\"}";
        ResultActions result = mockMvc.perform(post("/api/webhooks/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Signature", validSignature)
                .content(body)
                .with(csrf()));
        result.andExpect(status().isOk());
        // No duplicate enrollment
        verify(enrollmentRepo, never()).save(any());
    }

    @Test
    @WithMockUser(username = "webhook", roles = {"ADMIN"})
    void webhookFailedSetsPaymentFailed() throws Exception {
        when(paymentRepo.findById(eq(100L))).thenReturn(Optional.of(payment));
        String body = "{\"paymentId\":100,\"gatewayTxnId\":\"gw123\",\"status\":\"FAILED\"}";
        ResultActions result = mockMvc.perform(post("/api/webhooks/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Signature", validSignature)
                .content(body)
                .with(csrf()));
        result.andExpect(status().isOk());
        // Payment should be marked FAILED
        assertThat(payment.getStatus()).isEqualTo(Payment.PaymentStatus.FAILED);
        // No enrollment should be created
        verify(enrollmentRepo, never()).save(any());
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
