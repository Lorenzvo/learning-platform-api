-- Seed: 4 demo instructors and 4 courses (Digital Marketing, Graphic Design, Linear Algebra, Cloud Computing)
-- Safe to re-run: each block uses "insert if not exists" patterns.

-- 1) USERS for instructors (role doesn't matter for teaching assignment; 'USER' is fine)
--    Required by instructors.user_id (NOT NULL).
INSERT INTO users (email, password_hash, role, created_at)
SELECT  'ava.thompson@seed.example', '$2a$10$seedseedseedseedseedseedu9x0m2D9Nn1yq1', 'USER', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'ava.thompson@seed.example');

INSERT INTO users (email, password_hash, role, created_at)
SELECT 'noah.patel@seed.example', '$2a$10$seedseedseedseedseedseedu9x0m2D9Nn1yq1', 'USER', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'noah.patel@seed.example');

INSERT INTO users (email, password_hash, role, created_at)
SELECT 'maya.chen@seed.example', '$2a$10$seedseedseedseedseedseedu9x0m2D9Nn1yq1', 'USER', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'maya.chen@seed.example');

INSERT INTO users (email, password_hash, role, created_at)
SELECT 'liam.robinson@seed.example', '$2a$10$seedseedseedseedseedseedu9x0m2D9Nn1yq1', 'USER', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'liam.robinson@seed.example');

-- 2) INSTRUCTORS (link to the users above)
INSERT INTO instructors (user_id, name, bio, avatar_url)
SELECT u.id, 'Ava Thompson',
       'Digital strategist with 8+ years helping startups grow via SEO, paid media, and content funnels. Ex-agency lead.',
       NULL
FROM users u
WHERE u.email = 'ava.thompson@seed.example'
  AND NOT EXISTS (SELECT 1 FROM instructors i WHERE i.user_id = u.id);

INSERT INTO instructors (user_id, name, bio, avatar_url)
SELECT u.id, 'Noah Patel',
       'Senior product designer focused on accessible, systems-driven UI. Loves grids, typography, and Figma auto-layout.',
       NULL
FROM users u
WHERE u.email = 'noah.patel@seed.example'
  AND NOT EXISTS (SELECT 1 FROM instructors i WHERE i.user_id = u.id);

INSERT INTO instructors (user_id, name, bio, avatar_url)
SELECT u.id, 'Maya Chen',
       'Applied mathematician and lecturer. Teaches linear algebra with intuition-first visuals and real-world examples.',
       NULL
FROM users u
WHERE u.email = 'maya.chen@seed.example'
  AND NOT EXISTS (SELECT 1 FROM instructors i WHERE i.user_id = u.id);

INSERT INTO instructors (user_id, name, bio, avatar_url)
SELECT u.id, 'Liam Robinson',
       'Cloud architect (AWS/GCP) focused on scalable infra, IaC, and cost-aware design. Former SRE.',
       NULL
FROM users u
WHERE u.email = 'liam.robinson@seed.example'
  AND NOT EXISTS (SELECT 1 FROM instructors i WHERE i.user_id = u.id);

-- 3) COURSES
-- Note: Set created_at/updated_at explicitly for portability even if defaults exist.

-- Digital Marketing (BEGINNER)
INSERT INTO courses
(slug, title, short_description, description, price_cents, currency, is_active, level, thumbnail_url, instructor_id, created_at, updated_at)
SELECT
    'digital-marketing',
    'Digital Marketing',
    'Master digital marketing strategies to grow your online presence.',
    'Learn practical digital marketing: SEO fundamentals, on-page optimization, content strategy, paid search/social basics, simple analytics (UTM, funnels), and landing page best practices. Build a small campaign end-to-end and measure results.',
    5999, 'USD', TRUE, 'BEGINNER',
    'https://www.reshot.com/preview-assets/illustrations/FHZSTMBAP2/digital-marketing-team-FHZSTMBAP2-w1600.jpg',
    (SELECT i.id FROM instructors i WHERE i.name = 'Ava Thompson'),
    NOW(), NOW()
    WHERE NOT EXISTS (SELECT 1 FROM courses WHERE slug = 'digital-marketing');

-- Graphic Design (BEGINNER)
INSERT INTO courses
(slug, title, short_description, description, price_cents, currency, is_active, level, thumbnail_url, instructor_id, created_at, updated_at)
SELECT
    'graphic-design',
    'Graphic Design',
    'Learn the principles of graphic design and visual communication.',
    'A foundations course in visual design: layout and hierarchy, color and contrast, typography, spacing, and components. Practice with quick exercises and export assets for web/social. Includes a mini portfolio project.',
    5499, 'USD', TRUE, 'BEGINNER',
    'https://miro.medium.com/0*G_W4PEC6F5eZePDU.jpg',
    (SELECT i.id FROM instructors i WHERE i.name = 'Noah Patel'),
    NOW(), NOW()
    WHERE NOT EXISTS (SELECT 1 FROM courses WHERE slug = 'graphic-design');

-- Linear Algebra (INTERMEDIATE)
INSERT INTO courses
(slug, title, short_description, description, price_cents, currency, is_active, level, thumbnail_url, instructor_id, created_at, updated_at)
SELECT
    'linear-algebra',
    'Linear Algebra',
    'Understand the foundations of linear algebra for STEM fields.',
    'Vectors, matrices, linear transformations, rank, determinants, eigenvalues/eigenvectors, and orthogonality—explained with geometric intuition and applications in data science and graphics. Recommended after a calculus intro.',
    6999, 'USD', TRUE, 'INTERMEDIATE',
    'https://r2.erweima.ai/i/CtY2uEsGS167dI6DXiV7vQ.png',
    (SELECT i.id FROM instructors i WHERE i.name = 'Maya Chen'),
    NOW(), NOW()
    WHERE NOT EXISTS (SELECT 1 FROM courses WHERE slug = 'linear-algebra');

-- Cloud Computing (BEGINNER)
INSERT INTO courses
(slug, title, short_description, description, price_cents, currency, is_active, level, thumbnail_url, instructor_id, created_at, updated_at)
SELECT
    'cloud-computing',
    'Cloud Computing',
    'Explore cloud platforms and learn how to deploy scalable applications.',
    'Learn core cloud concepts (compute, storage, networking), managed databases, IAM, monitoring, and cost basics. Deploy a simple web app with IaC (intro) and understand shared responsibility and fault tolerance.',
    6499, 'USD', TRUE, 'BEGINNER',
    'https://www.openaccessgovernment.org/wp-content/uploads/2022/09/Server-Illustration.png',
    (SELECT i.id FROM instructors i WHERE i.name = 'Liam Robinson'),
    NOW(), NOW()
    WHERE NOT EXISTS (SELECT 1 FROM courses WHERE slug = 'cloud-computing');

-- Update thumbnails for existing courses
UPDATE courses
SET thumbnail_url = 'https://howtodoinjava.com/wp-content/uploads/2019/03/JAVA.jpg'
WHERE slug = 'java-fundamentals';

UPDATE courses
SET thumbnail_url = 'https://www.appletechsoft.com/wp-content/uploads/2022/08/Fundamentals-For-Building-A-Great-Web-Design.jpg'
WHERE slug = 'web-development-bootcamp';