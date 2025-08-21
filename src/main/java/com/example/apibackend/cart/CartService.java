package com.example.apibackend.cart;

import com.example.apibackend.enrollment.EnrollmentRepository;
import com.example.apibackend.course.CourseRepository;
import com.example.apibackend.user.UserRepository;
import com.example.apibackend.user.User;
import com.example.apibackend.course.Course;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private static final Logger logger = LoggerFactory.getLogger(CartService.class);

    /**
     * Adds a course to the user's cart, blocking if already enrolled or already in cart.
     */
    @Transactional
    public void addCourseToCart(Long userId, Long courseId) {
        Cart cart = cartRepository.findByUserId(userId).orElse(null);
        if (cart == null) {
            cart = new Cart();
            cart.setUserId(userId);
            cart.setItems(new java.util.ArrayList<>());
            cart = cartRepository.save(cart);
            logger.info("Created new cart for user {}", userId);
        }
        if (enrollmentRepository.existsByUserIdAndCourseId(userId, courseId)) {
            logger.warn("User {} is already enrolled in course {}. Cannot add to cart.", userId, courseId);
            throw new IllegalStateException("You are already enrolled in this course.");
        }
        // Check directly in the database for cart item existence
        boolean alreadyInCart = cartItemRepository.existsByCartIdAndCourseId(cart.getId(), courseId);
        if (alreadyInCart) {
            logger.warn("Course {} is already in user {}'s cart.", courseId, userId);
            throw new IllegalStateException("Course is already in your cart.");
        }
        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setCourseId(courseId);
        cartItemRepository.save(cartItem);
        logger.info("Course {} added to user {}'s cart.", courseId, userId);
    }

    /**
     * Removes a course from the user's cart.
     */
    @Transactional
    public void removeCourseFromCart(Long userId, Long courseId) {
        Cart cart = cartRepository.findByUserId(userId).orElseThrow(() -> new IllegalArgumentException("Cart not found"));
        CartItem item = cartItemRepository.findByCartIdAndCourseId(cart.getId(), courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found in cart"));
        cartItemRepository.delete(item);
        // Remove from cart's items list and save cart to trigger orphan removal
        if (cart.getItems() != null) {
            cart.getItems().removeIf(ci -> ci.getId().equals(item.getId()));
            cartRepository.save(cart);
        }
        logger.info("Course {} removed from user {}'s cart.", courseId, userId);
    }

    /**
     * Returns all items in the user's cart as DTOs for frontend.
     */
    @Transactional(readOnly = true)
    public java.util.List<CartItemDto> getCartItems(Long userId) {
        Cart cart = cartRepository.findByUserId(userId).orElse(null);
        if (cart == null) {
            return java.util.Collections.emptyList();
        }
        var cartItems = cartItemRepository.findByCartId(cart.getId());
        java.util.List<CartItemDto> result = new java.util.ArrayList<>();
        for (CartItem item : cartItems) {
            Course course = courseRepository.findById(item.getCourseId()).orElse(null);
            if (course != null) {
                result.add(new CartItemDto(
                    course.getId(),
                    course.getTitle(),
                    course.getPriceCents(),
                    course.getThumbnailUrl() // can be null
                ));
            }
        }
        return result;
    }

    /**
     * Clears all items from the user's cart.
     */
    @Transactional
    public void clearCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId).orElse(null);
        if (cart != null) {
            logger.info("Before clear: cart items count = {}", cart.getItems() != null ? cart.getItems().size() : 0);
            // Remove all items from the cart's items list and save the cart (triggers orphan removal)
            if (cart.getItems() != null && !cart.getItems().isEmpty()) {
                cart.getItems().clear();
                cartRepository.save(cart);
                logger.info("After clear: cart items count = {}", cart.getItems().size());
            }
            // Also run bulk delete for safety (in case items are not loaded in memory)
            cartItemRepository.deleteByCartId(cart.getId());
            logger.info("After bulk delete: cart items count = {}", cartItemRepository.findByCartId(cart.getId()).size());
            logger.info("Cleared all items from cart for user {} (orphanRemoval + bulk delete)", userId);
        }
    }
}
