package com.example.apibackend.course;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.DynamicPropertyRegistry;

import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.MySQLContainer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository slice test:
 * - Loads JPA/Hibernate only (no web tier).
 * - Uses Testcontainers MySQL to match production dialect/features (ENUM, TEXT).
 * - Applies Flyway migrations automatically on context load.
 */

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // don't swap to H2
@ActiveProfiles("test")
@Testcontainers
class CourseRepositoryTest {

    // Start a MySQL 8 container for this test class
    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("learning")
            .withUsername("app")
            .withPassword("app");

    static {
        mysql.start();
    }

    // Point Spring to the container's JDBC settings at runtime
    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", mysql::getJdbcUrl);
        r.add("spring.datasource.username", mysql::getUsername);
        r.add("spring.datasource.password", mysql::getPassword);
        // Ensure Flyway runs against the container
        r.add("spring.flyway.enabled", () -> true);
    }

    @Autowired
    CourseRepository repo;

    @BeforeEach
    void seed() {
        // Clean slate (idempotent for safety)
        repo.deleteAll();

        Course c1 = new Course();
        c1.setSlug("java-basics");
        c1.setTitle("Java Basics");
        c1.setDescription("Intro to Java");
        c1.setLevel("BEGINNER");
        c1.setPriceCents(2999);
        c1.setCurrency("USD");
        c1.setIsActive(true);
        repo.save(c1);

        Course c2 = new Course();
        c2.setSlug("spring-boot-fundamentals");
        c2.setTitle("Spring Boot Fundamentals");
        c2.setDescription("Spring Boot REST");
        c2.setLevel("BEGINNER");
        c2.setPriceCents(4999);
        c2.setCurrency("USD");
        c2.setIsActive(true);
        repo.save(c1);
        repo.save(c2);
    }

    @Test
    @DisplayName("findBySlugAndIsActiveTrue returns course when active")
    void findBySlugAndIsActiveTrue_ok() {
        var found = repo.findBySlugAndIsActiveTrue("java-basics");
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Java Basics");
    }

    @Test
    @DisplayName("findBySlugAndIsActiveTrue returns empty for missing slug")
    void findBySlugAndIsActiveTrue_404() {
        var missing = repo.findBySlugAndIsActiveTrue("nope");
        assertThat(missing).isEmpty();
    }

    /**
     * NEW: Test custom search method with filters and pagination.
     * Verifies DB-level filtering and paging for GET /api/courses.
     */
    @Test
    @DisplayName("search() returns filtered and paged results")
    void search_returns_filtered_and_paged() {
        // Act: search for 'java' in title, level BEGINNER, published true
        org.springframework.data.domain.Page<Course> page = repo.search(
            "java", "BEGINNER", true,
            org.springframework.data.domain.PageRequest.of(0, 10)
        );

        // Assert: only c1 matches
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getSlug()).isEqualTo("java-basics");
    }

    @AfterEach
    void cleanup() {
        repo.deleteAll();
    }
}