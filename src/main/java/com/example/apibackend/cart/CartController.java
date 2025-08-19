package com.example.apibackend.cart;

import com.example.apibackend.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    /**
     * Adds a course to the user's cart.
     */
    @PostMapping("/add")
    public ResponseEntity<?> addCourseToCart(
            @RequestParam Long courseId,
            @AuthenticationPrincipal User user
    ) {
        cartService.addCourseToCart(user.getId(), courseId);
        return ResponseEntity.ok().body("Course added to cart");
    }

    /**
     * Removes a course from the user's cart.
     */
    @DeleteMapping("/remove")
    public ResponseEntity<?> removeCourseFromCart(
            @RequestParam Long courseId,
            @AuthenticationPrincipal User user
    ) {
        cartService.removeCourseFromCart(user.getId(), courseId);
        return ResponseEntity.ok().body("Course removed from cart");
    }
}

