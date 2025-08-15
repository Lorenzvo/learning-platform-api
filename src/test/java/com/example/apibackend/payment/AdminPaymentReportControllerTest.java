package com.example.apibackend.payment;

import com.example.apibackend.course.Course;
import com.example.apibackend.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminPaymentController.class)
class AdminPaymentReportControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private PaymentRepository paymentRepo;

    private Payment payment;
    private User user;
    private Course course;

    @BeforeEach
    void setup() {
        user = new User();
        setId(user, 1L);
        user.setEmail("user@example.com");
        course = new Course();
        setId(course, 2L);
        course.setSlug("test-course");
        payment = new Payment();
        setId(payment, 3L);
        payment.setUser(user);
        payment.setCourse(course);
        payment.setAmountCents(1000);
        payment.setCurrency("USD");
        payment.setStatus(Payment.PaymentStatus.SUCCESS);
        payment.setGatewayTxnId("txn-123");
        // Set createdAt to a known date
        Instant instant = LocalDate.of(2025, 8, 10).atStartOfDay().toInstant(ZoneOffset.UTC);
        setCreatedAt(payment, instant);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void exportPaymentsCsv_withData_returnsCsv() throws Exception {
        // Payment in window
        when(paymentRepo.findAll()).thenReturn(Arrays.asList(payment));
        mockMvc.perform(get("/api/admin/payments/reports/payments.csv")
                .param("from", "2025-08-01")
                .param("to", "2025-08-15")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("paymentId,userEmail,courseSlug,amount,currency,status,gatewayTxnId,createdAt")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("3,user@example.com,test-course,1000,USD,SUCCESS,txn-123")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void exportPaymentsCsv_noData_returnsHeaderOnly() throws Exception {
        // No payments in window
        when(paymentRepo.findAll()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/admin/payments/reports/payments.csv")
                .param("from", "2025-08-01")
                .param("to", "2025-08-15")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("paymentId,userEmail,courseSlug,amount,currency,status,gatewayTxnId,createdAt")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("user@example.com"))));
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

    // Helper to set private createdAt field
    private void setCreatedAt(Object entity, Instant createdAt) {
        try {
            var field = entity.getClass().getDeclaredField("createdAt");
            field.setAccessible(true);
            field.set(entity, createdAt);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
