package com.example.apibackend.payment;

import com.example.apibackend.cart.Cart;
import com.example.apibackend.cart.CartItem;
import com.example.apibackend.cart.CartRepository;
import com.example.apibackend.course.Course;
import com.example.apibackend.course.CourseRepository;
import com.example.apibackend.user.User;
import com.example.apibackend.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(PaymentController.class)
class CartCheckoutControllerTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean PaymentService paymentService;
    @MockitoBean CartRepository cartRepo;
    @MockitoBean CourseRepository courseRepo;
    @MockitoBean UserRepository userRepo;

    private User user;
    private Cart cart;
    private Course course1, course2;
    private CartItem item1, item2;

    @BeforeEach
    void setup() {
        user = new User();
        setId(user, 1L);
        user.setEmail("Test@gmail.com");
        user.setPasswordHash("password");

        cart = new Cart();
        setId(cart, 10L);
        cart.setUserId(1L);

        course1 = new Course();
        setId(course1, 101L);
        course1.setTitle("Course 1");
        course1.setPriceCents(1000);
        course1.setCurrency("USD");
        course1.setIsActive(true);

        course2 = new Course();
        setId(course2, 102L);
        course2.setTitle("Course 2");
        course2.setPriceCents(2000);
        course2.setCurrency("USD");
        course2.setIsActive(true);

        item1 = new CartItem();
        setId(item1, 201L);
        item1.setCart(cart);
        item1.setCourseId(101L);

        item2 = new CartItem();
        setId(item2, 202L);
        item2.setCart(cart);
        item2.setCourseId(102L);

        cart.setItems(Arrays.asList(item1, item2));
        when(cartRepo.findByUserId(eq(1L))).thenReturn(Optional.of(cart));
        when(courseRepo.findById(eq(101L))).thenReturn(Optional.of(course1));
        when(courseRepo.findById(eq(102L))).thenReturn(Optional.of(course2));
    }

    @Test
    @WithMockUser(username = "1", roles = {"USER"})
    void checkoutCart_returnsPaymentsArray() throws Exception {
        // Simulate service returns payments for both items
        CartCheckoutResponseDTO.CartPaymentDTO payment1 = new CartCheckoutResponseDTO.CartPaymentDTO(301L, 101L, 1000, "USD", "PENDING", "cs_test_301");
        CartCheckoutResponseDTO.CartPaymentDTO payment2 = new CartCheckoutResponseDTO.CartPaymentDTO(302L, 102L, 2000, "USD", "PENDING", "cs_test_302");
        CartCheckoutResponseDTO responseDTO = new CartCheckoutResponseDTO(Arrays.asList(payment1, payment2));
        when(paymentService.createOrGetPendingPaymentsForCart(eq(1L))).thenReturn(responseDTO);
        ResultActions result = mockMvc.perform(post("/api/checkout/cart").with(csrf()));
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.payments").isArray())
                .andExpect(jsonPath("$.payments.length()").value(2))
                .andExpect(jsonPath("$.payments[0].status").value("PENDING"))
                .andExpect(jsonPath("$.payments[1].status").value("PENDING"));
    }

    @Test
    @WithMockUser(username = "1", roles = {"USER"})
    void checkoutCart_idempotent_returnsSamePayments() throws Exception {
        // Simulate service returns same payments for rapid retry
        CartCheckoutResponseDTO.CartPaymentDTO payment1 = new CartCheckoutResponseDTO.CartPaymentDTO(301L, 101L, 1000, "USD", "PENDING", "cs_test_301");
        CartCheckoutResponseDTO.CartPaymentDTO payment2 = new CartCheckoutResponseDTO.CartPaymentDTO(302L, 102L, 2000, "USD", "PENDING", "cs_test_302");
        CartCheckoutResponseDTO responseDTO = new CartCheckoutResponseDTO(Arrays.asList(payment1, payment2));
        when(paymentService.createOrGetPendingPaymentsForCart(eq(1L))).thenReturn(responseDTO);
        // First call
        ResultActions result1 = mockMvc.perform(post("/api/checkout/cart").with(csrf()));
        result1.andExpect(status().isOk())
                .andExpect(jsonPath("$.payments.length()").value(2));
        // Second call (rapid retry)
        ResultActions result2 = mockMvc.perform(post("/api/checkout/cart").with(csrf()));
        result2.andExpect(status().isOk())
                .andExpect(jsonPath("$.payments.length()").value(2));
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

