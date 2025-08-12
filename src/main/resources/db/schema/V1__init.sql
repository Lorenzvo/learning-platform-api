-- V1: Initial schema for users, courses, enrollments
-- Convention: BIGINT AUTO_INCREMENT PK, monetary amounts in cents (INT)

-- USERS: authentication + authorization (role)
CREATE TABLE users (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT,                  -- surrogate PK
                       email VARCHAR(255) UNIQUE NOT NULL,                    -- login + unique identity
                       password_hash VARCHAR(255) NOT NULL,                   -- BCrypt hash (never store plaintext)
                       role VARCHAR(20) NOT NULL,                             -- 'USER' or 'ADMIN'
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP         -- audit (creation time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- COURSES: catalog items students can purchase
CREATE TABLE courses (
                         id BIGINT PRIMARY KEY AUTO_INCREMENT,
                         slug VARCHAR(255) UNIQUE NOT NULL,                     -- SEO/key for URLs (e.g., "java-basics")
                         title VARCHAR(255) NOT NULL,                           -- course title
                         description TEXT,                                      -- long description/HTML allowed
                         price_cents INT NOT NULL,                              -- price in minor units, avoids float rounding
                         is_active TINYINT(1) NOT NULL DEFAULT 1,               -- soft-activation flag (1=true)
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ENROLLMENTS: which user owns which course
CREATE TABLE enrollments (
                             id BIGINT PRIMARY KEY AUTO_INCREMENT,
                             user_id BIGINT NOT NULL,                               -- FK → users.id
                             course_id BIGINT NOT NULL,                             -- FK → courses.id
                             status ENUM('PENDING','ACTIVE','CANCELED') NOT NULL DEFAULT 'PENDING',
                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             UNIQUE KEY uq_enroll (user_id, course_id),             -- prevent duplicate enrollments
                             CONSTRAINT fk_enroll_user   FOREIGN KEY (user_id)  REFERENCES users(id),
                             CONSTRAINT fk_enroll_course FOREIGN KEY (course_id) REFERENCES courses(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- NOTE:
-- • We’ll add orders, order_items, and webhook_event_log in a later migration (V2+).
-- • Use utf8mb4 for full emoji/symbol support.
-- • Keep monetary values in INT (cents) to avoid floating-point issues.
