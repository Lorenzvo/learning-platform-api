package com.example.apibackend.e2e;

import com.example.apibackend.course.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.MySQLContainer;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.DynamicPropertyRegistry;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end test:
 * - Boots the whole Spring Boot app on a RANDOM_PORT.
 * - Uses Testcontainers MySQL (Flyway runs on startup).
 * - Calls real HTTP endpoints via TestRestTemplate.
 * - Bypasses security for tests (permit all).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
class CourseApiIT {

    // MySQL container for the whole test class
    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("learning")
            .withUsername("app")
            .withPassword("app");

    static { mysql.start(); }

    // Inject container JDBC props into Spring at runtime
    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", mysql::getJdbcUrl);
        r.add("spring.datasource.username", mysql::getUsername);
        r.add("spring.datasource.password", mysql::getPassword);
        r.add("spring.flyway.enabled", () -> true);
        r.add("spring.flyway.locations", () -> "classpath:db/schema");
    }

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    @Autowired
    CourseRepository courseRepo;

    @BeforeEach
    void seedData() {
        // clean slate to avoid cross-test pollution
        courseRepo.deleteAll();

        var c1 = new com.example.apibackend.course.Course();
        c1.setSlug("java-basics");
        c1.setTitle("Java Basics");
        c1.setDescription("Intro");
        c1.setPriceCents(2999);
        c1.setIsActive(true);

        var c2 = new com.example.apibackend.course.Course();
        c2.setSlug("spring-boot-fundamentals");
        c2.setTitle("Spring Boot Fundamentals");
        c2.setDescription("REST, JPA");
        c2.setPriceCents(4999);
        c2.setIsActive(true);

        courseRepo.saveAll(java.util.List.of(c1, c2));
    }


    @Test
    void health_is_up() {
        ResponseEntity<String> res = rest.getForEntity(url("/actuator/health"), String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).contains("\"status\":\"UP\"");
    }

    @Test
    void list_courses_has_seeded_data() {
        ResponseEntity<String> res = rest.getForEntity(url("/api/courses"), String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).contains("java-basics");
    }

    @Test
    void get_course_by_slug_ok_and_404() {
        ResponseEntity<String> ok = rest.getForEntity(url("/api/courses/java-basics"), String.class);
        assertThat(ok.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<String> missing = rest.getForEntity(url("/api/courses/nope"), String.class);
        assertThat(missing.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private String url(String path) { return "http://localhost:" + port + path; }
}