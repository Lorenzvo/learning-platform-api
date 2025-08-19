package com.example.apibackend.payment;

import com.example.apibackend.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    /**
     * Handles single-course purchase checkout.
     * Accepts courseId, infers userId from JWT, and returns payment details.
     * <p>
     * For multi-course/cart, refactor to accept a list of courseIds.
     * For Stripe/Razorpay, replace clientSecret generation and integrate gateway API.
     */

    @PostMapping
    public ResponseEntity<CheckoutResponseDTO> checkout(
            @RequestParam Long courseId,
            @AuthenticationPrincipal User user // Spring Security injects authenticated user
    ) {
        // Extract userId from JWT principal
        Long userId = user.getId();
        // Call service to create or get PENDING payment
        CheckoutResponseDTO dto = paymentService.createOrGetPendingPayment(userId, courseId);
        return ResponseEntity.ok(dto);
    }

    /**
     * Handles cart checkout for multi-course purchase.
     * Accepts user token, finds user's cart, and creates one PENDING payment per course.
     * Returns array of payment DTOs. Multiple payments are acceptable for MVP simplicity.
     * <p>
     * In future, a payment_items table would allow a single charge for all cart items.
     */
    @PostMapping("/cart")
    public ResponseEntity<CartCheckoutResponseDTO> checkoutCart(
            @AuthenticationPrincipal User user
    ) {
        Long userId = user.getId();
        CartCheckoutResponseDTO dto = paymentService.createOrGetPendingPaymentsForCart(userId);
        return ResponseEntity.ok(dto);
    }
}

