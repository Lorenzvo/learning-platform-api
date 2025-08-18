package com.example.apibackend.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    // Optionally: find all valid tokens for a user
    long countByUserIdAndUsedFalseAndExpiresAtAfter(Long userId, java.time.LocalDateTime now);
}

