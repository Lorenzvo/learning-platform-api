package com.example.apibackend.instructor;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface InstructorRepository extends JpaRepository<Instructor, Long> {
    List<Instructor> findAll();
    Optional<Instructor> findById(Long id);
    // Optionally, add findByUserId if needed
}

