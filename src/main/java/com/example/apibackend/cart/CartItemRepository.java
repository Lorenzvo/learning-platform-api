package com.example.apibackend.cart;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCartId(Long cartId);
    boolean existsByCartIdAndCourseId(Long cartId, Long courseId);
}

