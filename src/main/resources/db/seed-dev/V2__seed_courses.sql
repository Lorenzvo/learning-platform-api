-- Seed a couple of example courses so the frontend has something to render
INSERT INTO courses (slug, title, description, price_cents, is_active)
VALUES
    ('java-basics','Java Basics','Intro to Java syntax, OOP, tooling', 2999, 1),
    ('spring-boot-fundamentals','Spring Boot Fundamentals','REST, JPA, profiles, testing', 4999, 1);