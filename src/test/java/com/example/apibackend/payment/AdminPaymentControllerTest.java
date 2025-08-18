package com.example.apibackend.payment;

import com.example.apibackend.enrollment.EnrollmentRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@WebMvcTest(AdminPaymentController.class)
class AdminPaymentControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;
    @MockitoBean
    private PaymentRepository paymentRepository;
    @MockitoBean
    private EnrollmentRepository enrollmentRepository;

    // Helper to set up a valid paymentId
    private static final Long TEST_PAYMENT_ID = 42L;

    @Test
    @WithMockUser(roles = "ADMIN")
    void refundPayment_success_returnsOk() throws Exception {
        // Arrange: mock service to do nothing (void)
        Mockito.doNothing().when(paymentService).refundPayment(TEST_PAYMENT_ID);
        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/payments/{paymentId}/refund", TEST_PAYMENT_ID))
                .andExpect(MockMvcResultMatchers.status().isOk());
        // Verify service called
        Mockito.verify(paymentService).refundPayment(TEST_PAYMENT_ID);
    }

    @Test
    @WithMockUser(roles = "USER")
    void refundPayment_nonAdmin_forbidden() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/payments/{paymentId}/refund", TEST_PAYMENT_ID))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }
}

