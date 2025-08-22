Overview

Learning Platform API — a Spring Boot REST API powering an e‑learning app: authentication (JWT), course catalogue, modules/lessons, carts, Stripe payments (single & cart), webhooks, enrollments, reviews, and admin operations. Data stored in MySQL, migrations via Flyway.

Tech Stack

Java 17+, Spring Boot

MySQL 8, Flyway

Stripe (PaymentIntents + webhooks)

JWT auth (bcrypt hashed passwords)

(Optional) Docker for local DB

Project Structure (high level)
src/main/java/com/example/apibackend/
  ├─ auth/ (AuthController, JwtAuthFilter, JwtUtil, SecurityConfig)
  ├─ course/ (Course, CourseController, AdminCourseController, repositories, DTOs)
  ├─ module/ (Module, AdminModuleController, repositories, DTOs)
  ├─ lesson/ (Lesson, LessonDto, LessonRepository, LessonType)
  ├─ enrollment/ (Enrollment, EnrollmentController, AdminEnrollmentController, DevEnrollmentController)
  ├─ cart/ (Cart, CartItem, CartController, CartService, repositories)
  ├─ payment/ (Payment, PaymentItem, PaymentController, PaymentService, PaymentWebhookController, repositories)
  ├─ instructor/ (Instructor, InstructorController, repositories)
  ├─ review/ (Review, ReviewController, repositories)
  ├─ user/ (User, UserRepository, UserProfileController)
  └─ config/ (StripeConfig, WebConfig, etc.)
src/main/resources/
  ├─ application.yml / application-*.yml
  ├─ db/schema (Flyway versioned migrations)
  └─ db/seed-dev (dev‑only sample seed scripts)

Prerequisites

Java 17+

MySQL 8+ (or Docker)

Stripe CLI (for local webhooks)

(Optional) curl / Postman for testing

Environment Variables

Create src/main/resources/application.yml (or use application-dev.yml) and supply secrets via env:

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/learn_platform?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    username: ${DB_USER:root}
    password: ${DB_PASS:root}
  jpa:
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true
    # In dev, add seed-dev location (via profile or override)
    locations: classpath:db/schema,classpath:db/seed-dev

app:
  jwt:
    secret: ${JWT_SECRET:replace-this-dev-secret}
    expiresInSeconds: 3600

stripe:
  secretKey: ${STRIPE_SECRET_KEY:sk_test_xxx}
  webhookSecret: ${STRIPE_WEBHOOK_SECRET:whsec_xxx}


Tip (dev profile): keep db/seed-dev enabled only for spring.profiles.active=dev.

.env (shell) example
export DB_USER=root
export DB_PASS=root
export JWT_SECRET=dev_jwt_secret_change_me
export STRIPE_SECRET_KEY=sk_test_123
export STRIPE_WEBHOOK_SECRET=whsec_123
export SPRING_PROFILES_ACTIVE=dev

Database Setup
Option A: Local MySQL
CREATE DATABASE learn_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'learn_user'@'%' IDENTIFIED BY 'learn_pass';
GRANT ALL PRIVILEGES ON learn_platform.* TO 'learn_user'@'%';
FLUSH PRIVILEGES;


Update application.yml datasource accordingly.

Option B: Docker MySQL
docker run --name mysql8-learn -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=learn_platform \
  -p 3306:3306 -d mysql:8

Build & Run
./mvnw clean spring-boot:run
# or
./mvnw package && java -jar target/*.jar


The API should be up at http://localhost:8080.

Stripe Dev Workflow

Start the API.

Run Stripe CLI to forward events locally:

stripe listen --forward-to http://localhost:8080/api/webhooks/payment
# copy the printed whsec_* into STRIPE_WEBHOOK_SECRET (dev)


Create PaymentIntents via:

POST /api/checkout (single course)

POST /api/checkout/cart (cart of courses)

Confirm a PaymentIntent (test card):

stripe paymentintents confirm <pi_id> \
  --payment-method pm_card_visa \
  --return-url http://localhost:3000/stripe-return


Webhook marks Payment.SUCCESS and creates Enrollment.ACTIVE (idempotent).

Verify via GET /api/enrollments/me.

For no redirects dev flow, ensure you created PIs with automatic_payment_methods.allow_redirects=never on the server.

Key Endpoints (summary)

Auth: POST /api/auth/login (JWT), POST /api/auth/signup (optional)

Courses: GET /api/courses (paged + filters: q, level, minRating, sort), GET /api/courses/{slug} (detail/TOC)

Cart: GET /api/cart, POST /api/cart/items {courseId}, DELETE /api/cart/items/{id}

Payments: POST /api/checkout {courseId}, POST /api/checkout/cart → returns clientSecrets; webhook: POST /api/webhooks/payment

Enrollments: GET /api/enrollments/me

Lessons: (optional) GET /api/lessons/{lessonId}/playback-token (gated, short‑lived JWT)

Reviews: POST /api/reviews, GET /api/courses/{slug}/reviews (if exposed)

Admin: CRUD for courses/modules/lessons, payments CSV export, refunds

Seeding Dev Data

Instructors & Courses: db/seed-dev/VXX__seed_instructors_and_courses.sql (+ more seeds you added)

Modules/Lessons (demos): db/seed-dev/V__seed_modules_lessons_with_demos.sql

Additional 13 Users/Instructors/Courses: db/seed-dev/VXX__seed_13_users_instructors_courses.sql

Reviews 2–20: db/seed-dev/V__seed_reviews_courses_2_to_20.sql

Seeds are idempotent (WHERE NOT EXISTS) and designed for dev environments.

CORS & Dev Proxy

Either: enable CORS for http://localhost:5173 in SecurityConfig (dev only).

Or: use Vite dev proxy (recommended) so the frontend calls /api without CORS.

Error Model

Return structured errors:

{ "code": "PAYMENT_FAILED", "message": "Declined", "details": { "stripeCode": "card_declined" } }

Security Notes

Verify Stripe webhook signatures.

Use idempotency on both Stripe API calls and webhook processing (store processed event.id).

Never log full card data; avoid logging entire webhook payloads in prod.

Health

Expose only /actuator/health publicly; lock down /actuator/info or disable.

Deployment (brief)

Prod DB: MySQL RDS or managed MySQL.

App: Run as service on EC2; front with Nginx reverse proxy.

Secrets: Environment variables or Parameter Store/Secrets Manager.

CI/CD (future): Jenkins pipeline to build/test/deploy.
