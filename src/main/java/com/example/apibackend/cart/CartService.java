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
        if (enrollmentRepository.existsByUserIdAndCourseId(userId, courseId)) {
            logger.warn("User {} is already enrolled in course {}. Cannot add to cart.", userId, courseId);
            throw new IllegalStateException("You are already enrolled in this course.");
        }
        Cart cart = cartRepository.findByUserId(userId).orElseThrow(() -> new IllegalArgumentException("Cart not found"));
        boolean alreadyInCart = cart.getItems().stream().anyMatch(item -> item.getCourseId().equals(courseId));
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
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getCourseId().equals(courseId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Course not found in cart"));
        cartItemRepository.delete(item);
        logger.info("Course {} removed from user {}'s cart.", courseId, userId);
    }
}

