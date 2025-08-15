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
     *
     * For multi-course/cart, refactor to accept a list of courseIds.
     * For Stripe/Razorpay, replace clientSecret generation and integrate gateway API.
     */

    @PostMapping
    public ResponseEntity<CheckoutResponseDTO> checkout(
            @RequestParam Long courseId,
            @AuthenticationPrincipal UserDetails userDetails // Spring Security injects authenticated user
    ) {
        // Extract userId from JWT principal
        Long userId = extractUserId(userDetails);
        // Call service to create or get PENDING payment
        CheckoutResponseDTO dto = paymentService.createOrGetPendingPayment(userId, courseId);
        return ResponseEntity.ok(dto);
    }

    // Helper to extract userId from UserDetails (uses getId() on the principal if available)
    private Long extractUserId(UserDetails userDetails) {
        // If your UserDetails is actually your User entity, just call getId()
        // This works if you use @AuthenticationPrincipal User user in your controller
        try {
            // Attempt to cast to User and get ID
            if (userDetails instanceof User user) {
                return user.getId();
            }
            // Otherwise, fallback to username parsing (legacy)
            return Long.parseLong(userDetails.getUsername());
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to extract user ID from JWT principal");
        }
    }
}
