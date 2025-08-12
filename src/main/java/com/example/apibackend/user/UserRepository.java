package com.example.apibackend.user;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * CRUD and queries for User entities.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    // SELECT * FROM users WHERE email=? LIMIT 1
    Optional<User> findByEmail(String email);
}