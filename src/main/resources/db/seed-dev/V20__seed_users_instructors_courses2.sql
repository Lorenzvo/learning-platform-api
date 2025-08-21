-- Seed: 13 users -> 13 instructors -> 13 courses
-- Safe to re-run (WHERE NOT EXISTS guards). Adapt table/column names if yours differ.

-- ========== 1) USERS (email, password_hash, role, created_at) ==========
-- bcrypt hash is a harmless placeholder (users need not log in)
INSERT INTO users (email, password_hash, role, created_at)
SELECT 'olivia.hughes@seed.example', '$2a$10$seedseedseedseedseedseedu9x0m2D9Nn1yq1', 'USER', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM users WHERE email='olivia.hughes@seed.example');

INSERT INTO users (email, password_hash, role, created_at)
SELECT 'ethan.martinez@seed.example', '$2a$10$seedseedseedseedseedseedu9x0m2D9Nn1yq1', 'USER', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM users WHERE email='ethan.martinez@seed.example');

INSERT INTO users (email, password_hash, role, created_at)
SELECT 'sophia.ramirez@seed.example', '$2a$10$seedseedseedseedseedseedu9x0m2D9Nn1yq1', 'USER', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM users WHERE email='sophia.ramirez@seed.example');

INSERT INTO users (email, password_hash, role, created_at)
SELECT 'jacob.nguyen@seed.example', '$2a$10$seedseedseedseedseedseedu9x0m2D9Nn1yq1', 'USER', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM users WHERE email='jacob.nguyen@seed.example');

INSERT INTO users (email, password_hash, role, created_at)
SELECT 'amelia.brown@seed.example', '$2a$10$seedseedseedseedseedseedu9x0m2D9Nn1yq1', 'USER', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM users WHERE email='amelia.brown@seed.example');

INSERT INTO users (email, password_hash, role, created_at)
SELECT 'liam.khan@seed.example', '$2a$10$seedseedseedseedseedseedu9x0m2D9Nn1yq1', 'USER', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM users WHERE email='liam.khan@seed.example');

INSERT INTO users (email, password_hash, role, created_at)
SELECT 'ava.wilson@seed.example', '$2a$10$seedseedseedseedseedseedu9x0m2D9Nn1yq1', 'USER', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM users WHERE email='ava.wilson@seed.example');

INSERT INTO users (email, password_hash, role, created_at)
SELECT 'noah.singh@seed.example', '$2a$10$seedseedseedseedseedseedu9x0m2D9Nn1yq1', 'USER', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM users WHERE email='noah.singh@seed.example');

INSERT INTO users (email, password_hash, role, created_at)
SELECT 'mia.fernandez@seed.example', '$2a$10$seedseedseedseedseedseedu9x0m2D9Nn1yq1', 'USER', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM users WHERE email='mia.fernandez@seed.example');

INSERT INTO users (email, password_hash, role, created_at)
SELECT 'lucas.lee@seed.example', '$2a$10$seedseedseedseedseedseedu9x0m2D9Nn1yq1', 'USER', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM users WHERE email='lucas.lee@seed.example');

INSERT INTO users (email, password_hash, role, created_at)
SELECT 'ella.murphy@seed.example', '$2a$10$seedseedseedseedseedseedu9x0m2D9Nn1yq1', 'USER', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM users WHERE email='ella.murphy@seed.example');

INSERT INTO users (email, password_hash, role, created_at)
SELECT 'mason.adams@seed.example', '$2a$10$seedseedseedseedseedseedu9x0m2D9Nn1yq1', 'USER', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM users WHERE email='mason.adams@seed.example');

INSERT INTO users (email, password_hash, role, created_at)
SELECT 'chloe.tan@seed.example', '$2a$10$seedseedseedseedseedseedu9x0m2D9Nn1yq1', 'USER', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM users WHERE email='chloe.tan@seed.example');

-- ========== 2) INSTRUCTORS (user_id, name, bio, avatar_url=NULL) ==========
INSERT INTO instructors (user_id, name, bio, avatar_url)
SELECT u.id, 'Olivia Hughes',
       'Data scientist & educator focusing on Python, pandas, and visualization for practical analytics.',
       NULL
FROM users u WHERE u.email='olivia.hughes@seed.example'
               AND NOT EXISTS (SELECT 1 FROM instructors i WHERE i.user_id=u.id);

INSERT INTO instructors (user_id, name, bio, avatar_url)
SELECT u.id, 'Ethan Martinez',
       'Full-stack JS engineer; loves teaching modern JavaScript and frameworks with real-world projects.',
       NULL
FROM users u WHERE u.email='ethan.martinez@seed.example'
               AND NOT EXISTS (SELECT 1 FROM instructors i WHERE i.user_id=u.id);

INSERT INTO instructors (user_id, name, bio, avatar_url)
SELECT u.id, 'Sophia Ramirez',
       'PMP-certified project manager specializing in agile delivery and stakeholder communication.',
       NULL
FROM users u WHERE u.email='sophia.ramirez@seed.example'
               AND NOT EXISTS (SELECT 1 FROM instructors i WHERE i.user_id=u.id);

INSERT INTO instructors (user_id, name, bio, avatar_url)
SELECT u.id, 'Jacob Nguyen',
       'Backend dev & DBA; teaches SQL from fundamentals to query optimization with real datasets.',
       NULL
FROM users u WHERE u.email='jacob.nguyen@seed.example'
               AND NOT EXISTS (SELECT 1 FROM instructors i WHERE i.user_id=u.id);

INSERT INTO instructors (user_id, name, bio, avatar_url)
SELECT u.id, 'Amelia Brown',
       'ML practitioner with a focus on intuition-first teaching and ethical AI considerations.',
       NULL
FROM users u WHERE u.email='amelia.brown@seed.example'
               AND NOT EXISTS (SELECT 1 FROM instructors i WHERE i.user_id=u.id);

INSERT INTO instructors (user_id, name, bio, avatar_url)
SELECT u.id, 'Liam Khan',
       'Security engineer; makes cybersecurity approachable with hands-on labs and threat modeling.',
       NULL
FROM users u WHERE u.email='liam.khan@seed.example'
               AND NOT EXISTS (SELECT 1 FROM instructors i WHERE i.user_id=u.id);

INSERT INTO instructors (user_id, name, bio, avatar_url)
SELECT u.id, 'Ava Wilson',
       'Mobile dev & Flutter advocate; shipping cross-platform apps and teaching pragmatic patterns.',
       NULL
FROM users u WHERE u.email='ava.wilson@seed.example'
               AND NOT EXISTS (SELECT 1 FROM instructors i WHERE i.user_id=u.id);

INSERT INTO instructors (user_id, name, bio, avatar_url)
SELECT u.id, 'Noah Singh',
       'DevOps/SRE with a passion for IaC, CI/CD, and reliable, cost-aware cloud architectures.',
       NULL
FROM users u WHERE u.email='noah.singh@seed.example'
               AND NOT EXISTS (SELECT 1 FROM instructors i WHERE i.user_id=u.id);

INSERT INTO instructors (user_id, name, bio, avatar_url)
SELECT u.id, 'Mia Fernandez',
       'UX strategist and content designer; helps teams craft clear, human-centered product copy.',
       NULL
FROM users u WHERE u.email='mia.fernandez@seed.example'
               AND NOT EXISTS (SELECT 1 FROM instructors i WHERE i.user_id=u.id);

INSERT INTO instructors (user_id, name, bio, avatar_url)
SELECT u.id, 'Lucas Lee',
       'Technical communication coach; empowers engineers to present complex ideas simply.',
       NULL
FROM users u WHERE u.email='lucas.lee@seed.example'
               AND NOT EXISTS (SELECT 1 FROM instructors i WHERE i.user_id=u.id);

INSERT INTO instructors (user_id, name, bio, avatar_url)
SELECT u.id, 'Ella Murphy',
       'CPA & analyst; teaches financial accounting with spreadsheets and real cases.',
       NULL
FROM users u WHERE u.email='ella.murphy@seed.example'
               AND NOT EXISTS (SELECT 1 FROM instructors i WHERE i.user_id=u.id);

INSERT INTO instructors (user_id, name, bio, avatar_url)
SELECT u.id, 'Mason Adams',
       'Data viz lead; Tableau/Looker expert turning messy data into clear, persuasive visuals.',
       NULL
FROM users u WHERE u.email='mason.adams@seed.example'
               AND NOT EXISTS (SELECT 1 FROM instructors i WHERE i.user_id=u.id);

INSERT INTO instructors (user_id, name, bio, avatar_url)
SELECT u.id, 'Chloe Tan',
       'AI engineer; teaches gen‑AI fundamentals with prompt patterns and responsible usage.',
       NULL
FROM users u WHERE u.email='chloe.tan@seed.example'
               AND NOT EXISTS (SELECT 1 FROM instructors i WHERE i.user_id=u.id);

-- ========== 3) COURSES (slug unique; USD; levels & prices varied) ==========
-- Python for Data Science (BEGINNER)
INSERT INTO courses
(slug, title, short_description, description, price_cents, currency, is_active, level, thumbnail_url, instructor_id, created_at, updated_at)
SELECT
    'python-for-data-science', 'Python for Data Science',
    'Start analyzing data with Python, pandas, and plots.',
    'Learn Python basics, NumPy, pandas, data cleaning, and quick plots with Matplotlib. Build a mini analytics report by the end.',
    3999, 'USD', TRUE, 'BEGINNER',
    'https://st2.depositphotos.com/6027554/11051/v/450/depositphotos_110511142-stock-illustration-python-programming-language.jpg',
    (SELECT id FROM instructors WHERE name='Olivia Hughes'), NOW(), NOW()
    WHERE NOT EXISTS (SELECT 1 FROM courses WHERE slug='python-for-data-science');

-- JavaScript Essentials (BEGINNER)
INSERT INTO courses
(slug, title, short_description, description, price_cents, currency, is_active, level, thumbnail_url, instructor_id, created_at, updated_at)
SELECT
    'javascript-essentials', 'JavaScript Essentials',
    'Master the fundamentals of modern JavaScript.',
    'Variables, functions, arrays, objects, async patterns, and DOM basics. Includes hands-on exercises and a small project.',
    3499, 'USD', TRUE, 'BEGINNER',
    'https://img.freepik.com/free-vector/programmers-using-javascript-programming-language-computer-tiny-people-javascript-language-javascript-engine-js-web-development-concept-bright-vibrant-violet-isolated-illustration_335657-986.jpg?semt=ais_hybrid&w=740&q=80',
    (SELECT id FROM instructors WHERE name='Ethan Martinez'), NOW(), NOW()
    WHERE NOT EXISTS (SELECT 1 FROM courses WHERE slug='javascript-essentials');

-- Project Management Fundamentals (BEGINNER)
INSERT INTO courses
(slug, title, short_description, description, price_cents, currency, is_active, level, thumbnail_url, instructor_id, created_at, updated_at)
SELECT
    'project-management-fundamentals', 'Project Management Fundamentals',
    'Plan, track, and deliver projects with confidence.',
    'Intro to project lifecycles, scope, risk, agile basics, and communications. Build a simple plan and status report.',
    2999, 'USD', TRUE, 'BEGINNER',
    'https://static.vecteezy.com/system/resources/thumbnails/024/316/087/small_2x/project-management-marketing-analysis-and-development-online-successful-strategy-motivation-and-leadership-modern-flat-cartoon-style-illustration-on-white-background-vector.jpg',
    (SELECT id FROM instructors WHERE name='Sophia Ramirez'), NOW(), NOW()
    WHERE NOT EXISTS (SELECT 1 FROM courses WHERE slug='project-management-fundamentals');

-- SQL Fundamentals (BEGINNER)
INSERT INTO courses
(slug, title, short_description, description, price_cents, currency, is_active, level, thumbnail_url, instructor_id, created_at, updated_at)
SELECT
    'sql-fundamentals', 'SQL Fundamentals',
    'Write queries to answer real questions from data.',
    'SELECTs, JOINs, GROUP BY, window functions intro, and indexing basics with practice on a sample dataset.',
    3299, 'USD', TRUE, 'BEGINNER',
    'https://placehold.co/640x360?text=SQL+Fundamentals',
    (SELECT id FROM instructors WHERE name='Jacob Nguyen'), NOW(), NOW()
    WHERE NOT EXISTS (SELECT 1 FROM courses WHERE slug='sql-fundamentals');

-- Machine Learning Intro (INTERMEDIATE)
INSERT INTO courses
(slug, title, short_description, description, price_cents, currency, is_active, level, thumbnail_url, instructor_id, created_at, updated_at)
SELECT
    'machine-learning-intro', 'Machine Learning Intro',
    'Build intuition for common ML models and workflows.',
    'Supervised learning, train/test split, metrics, overfitting, and simple models in scikit-learn with practical notebooks.',
    7999, 'USD', TRUE, 'INTERMEDIATE',
    'https://static.vecteezy.com/system/resources/thumbnails/001/881/533/small/business-team-creating-artificial-intelligence-machine-learning-and-artificial-intelligence-concept-vector.jpg',
    (SELECT id FROM instructors WHERE name='Amelia Brown'), NOW(), NOW()
    WHERE NOT EXISTS (SELECT 1 FROM courses WHERE slug='machine-learning-intro');

-- Cybersecurity Basics (BEGINNER)
INSERT INTO courses
(slug, title, short_description, description, price_cents, currency, is_active, level, thumbnail_url, instructor_id, created_at, updated_at)
SELECT
    'cybersecurity-basics', 'Cybersecurity Basics',
    'Protect systems with essential security practices.',
    'Threat modeling, common attacks, least privilege, secure defaults, and incident basics with hands-on labs.',
    4499, 'USD', TRUE, 'BEGINNER',
    'https://static.vecteezy.com/system/resources/thumbnails/001/397/516/small_2x/cyber-security-illustration-free-vector.jpg',
    (SELECT id FROM instructors WHERE name='Liam Khan'), NOW(), NOW()
    WHERE NOT EXISTS (SELECT 1 FROM courses WHERE slug='cybersecurity-basics');

-- Flutter Mobile Apps (INTERMEDIATE)
INSERT INTO courses
(slug, title, short_description, description, price_cents, currency, is_active, level, thumbnail_url, instructor_id, created_at, updated_at)
SELECT
    'flutter-mobile-apps', 'Flutter Mobile Apps',
    'Build cross-platform apps with Flutter and Dart.',
    'Layouts, state management, navigation, HTTP, and deployment tips. Build a simple app end to end.',
    6999, 'USD', TRUE, 'INTERMEDIATE',
    'https://miro.medium.com/1*o3uWxNqRWqE8BooPMtmOMQ.jpeg',
    (SELECT id FROM instructors WHERE name='Ava Wilson'), NOW(), NOW()
    WHERE NOT EXISTS (SELECT 1 FROM courses WHERE slug='flutter-mobile-apps');

-- DevOps Foundations (INTERMEDIATE)
INSERT INTO courses
(slug, title, short_description, description, price_cents, currency, is_active, level, thumbnail_url, instructor_id, created_at, updated_at)
SELECT
    'devops-foundations', 'DevOps Foundations',
    'Ship faster with CI/CD and infrastructure as code.',
    'Pipelines, containers, IaC concepts, monitoring, and incident basics. Build a minimal CI pipeline.',
    7499, 'USD', TRUE, 'INTERMEDIATE',
    'https://www.varseno.com/wp-content/uploads/2025/05/7016019-scaled-1.jpg',
    (SELECT id FROM instructors WHERE name='Noah Singh'), NOW(), NOW()
    WHERE NOT EXISTS (SELECT 1 FROM courses WHERE slug='devops-foundations');

-- UX Writing (ADVANCED)
INSERT INTO courses
(slug, title, short_description, description, price_cents, currency, is_active, level, thumbnail_url, instructor_id, created_at, updated_at)
SELECT
    'ux-writing', 'UX Writing',
    'Craft clear, helpful microcopy for interfaces.',
    'Voice & tone, clarity, labels, empty states, and error messages. Critique patterns from real apps.',
    2899, 'USD', TRUE, 'ADVANCED',
    'https://www.uxdesigninstitute.com/blog/wp-content/uploads/2023/05/124_What_is_UX_writing_Illustration_blog.png',
    (SELECT id FROM instructors WHERE name='Mia Fernandez'), NOW(), NOW()
    WHERE NOT EXISTS (SELECT 1 FROM courses WHERE slug='ux-writing');

-- Public Speaking for Tech (INTERMEDIATE)
INSERT INTO courses
(slug, title, short_description, description, price_cents, currency, is_active, level, thumbnail_url, instructor_id, created_at, updated_at)
SELECT
    'public-speaking-for-tech', 'Public Speaking for Tech',
    'Present technical topics with confidence.',
    'Structure a talk, tell a story with data, handle Q&A, and practice delivery with feedback.',
    4599, 'USD', TRUE, 'INTERMEDIATE',
    'https://img.freepik.com/free-vector/conference-stage-flat-concept-with-woman-speaking-front-audience-vector-illustration_1284-81444.jpg?semt=ais_hybrid&w=740&q=80',
    (SELECT id FROM instructors WHERE name='Lucas Lee'), NOW(), NOW()
    WHERE NOT EXISTS (SELECT 1 FROM courses WHERE slug='public-speaking-for-tech');

-- Financial Accounting (BEGINNER)
INSERT INTO courses
(slug, title, short_description, description, price_cents, currency, is_active, level, thumbnail_url, instructor_id, created_at, updated_at)
SELECT
    'financial-accounting', 'Financial Accounting',
    'Read and interpret financial statements.',
    'Balance sheet, income statement, cash flows, and basic ratios with spreadsheet practice.',
    3799, 'USD', TRUE, 'BEGINNER',
    'https://cdni.iconscout.com/illustration/premium/thumb/financial-accounting-7076310-5752286.png',
    (SELECT id FROM instructors WHERE name='Ella Murphy'), NOW(), NOW()
    WHERE NOT EXISTS (SELECT 1 FROM courses WHERE slug='financial-accounting');

-- Data Visualization with Tableau (ADVANCED)
INSERT INTO courses
(slug, title, short_description, description, price_cents, currency, is_active, level, thumbnail_url, instructor_id, created_at, updated_at)
SELECT
    'data-viz-with-tableau', 'Data Visualization with Tableau',
    'Turn data into clear, convincing visuals.',
    'Connect data, build charts & dashboards, and share insights. Design principles for effective storytelling.',
    6399, 'USD', TRUE, 'ADVANCED',
    'https://static.tildacdn.one/tild3237-3264-4165-b361-306365656663/main_pic.jpg',
    (SELECT id FROM instructors WHERE name='Mason Adams'), NOW(), NOW()
    WHERE NOT EXISTS (SELECT 1 FROM courses WHERE slug='data-viz-with-tableau');

-- Generative AI Basics (ADVANCED)
INSERT INTO courses
(slug, title, short_description, description, price_cents, currency, is_active, level, thumbnail_url, instructor_id, created_at, updated_at)
SELECT
    'advanced-generative-ai', 'Advanced Generative AI',
    'Understand RAG and how to fine-tune LLMs for specific tasks.',
    'Tokenization, embeddings, prompt patterns, evaluation, and safety. Build small demos with APIs and fine-tune your own LLM.',
    11999, 'USD', TRUE, 'ADVANCED',
    'https://knowledge.wharton.upenn.edu/wp-content/uploads/2024/02/2.21.24-mack-institute-gen-ai-conference-GettyImages-1479449423-900x612.jpg',
    (SELECT id FROM instructors WHERE name='Chloe Tan'), NOW(), NOW()
    WHERE NOT EXISTS (SELECT 1 FROM courses WHERE slug='generative-ai-basics');