package com.example.apibackend.user;

import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * CRUD and queries for User entities.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    // SELECT * FROM users WHERE email=? LIMIT 1
    Optional<User> findByEmail(String email);

    // SELECT * FROM users WHERE email=? AND deleted_at IS NULL LIMIT 1
    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    boolean existsByEmail(@NotBlank String email);

    // SELECT * FROM users WHERE id=? AND deleted_at IS NULL
    Optional<User> findByIdAndDeletedAtIsNull(Long id);

    // SELECT COUNT(*) FROM users WHERE email=? AND deleted_at IS NULL
    boolean existsByEmailAndDeletedAtIsNull(@NotBlank String email);
}
