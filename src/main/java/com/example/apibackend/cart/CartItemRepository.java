package com.example.apibackend.cart;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCartId(Long cartId);
    boolean existsByCartIdAndCourseId(Long cartId, Long courseId);
    Optional<CartItem> findByCartIdAndCourseId(Long cartId, Long courseId);
}
