/* =========================
   JavaScript Essentials
   ========================= */
INSERT INTO modules (course_id, title, position, description)
SELECT c.id, 'JS Fundamentals', 1,
       'Variables, functions, arrays, and objects—the building blocks of JavaScript.'
FROM courses c
WHERE c.slug = 'javascript-essentials'
  AND NOT EXISTS (
    SELECT 1 FROM modules m WHERE m.course_id = c.id AND m.title = 'JS Fundamentals'
);

INSERT INTO lessons (module_id, title, type, content_url, duration_seconds, is_demo)
SELECT m.id, 'Intro to Variables', 'VIDEO',
       'https://cdn.example.com/demos/js/variables-intro.mp4', 300, 1
FROM modules m
         JOIN courses c ON c.id = m.course_id
WHERE c.slug = 'javascript-essentials' AND m.title = 'JS Fundamentals'
  AND NOT EXISTS (SELECT 1 FROM lessons l WHERE l.module_id = m.id AND l.title = 'Intro to Variables');


/* =========================
   Project Management Fundamentals
   ========================= */
INSERT INTO modules (course_id, title, position, description)
SELECT c.id, 'Project Planning', 1,
       'Scope, milestones, and risk management for successful delivery.'
FROM courses c
WHERE c.slug = 'project-management-fundamentals'
  AND NOT EXISTS (
    SELECT 1 FROM modules m WHERE m.course_id = c.id AND m.title = 'Project Planning'
);

INSERT INTO lessons (module_id, title, type, content_url, duration_seconds, is_demo)
SELECT m.id, 'Defining Project Scope', 'TEXT',
       'https://cdn.example.com/notes/pm/scope-definition.html', 240, 1
FROM modules m
         JOIN courses c ON c.id = m.course_id
WHERE c.slug = 'project-management-fundamentals' AND m.title = 'Project Planning'
  AND NOT EXISTS (SELECT 1 FROM lessons l WHERE l.module_id = m.id AND l.title = 'Defining Project Scope');


/* =========================
   SQL Fundamentals
   ========================= */
INSERT INTO modules (course_id, title, position, description)
SELECT c.id, 'SQL Basics', 1,
       'SELECT, JOIN, and GROUP BY—core query patterns for data analysis.'
FROM courses c
WHERE c.slug = 'sql-fundamentals'
  AND NOT EXISTS (
    SELECT 1 FROM modules m WHERE m.course_id = c.id AND m.title = 'SQL Basics'
);

INSERT INTO lessons (module_id, title, type, content_url, duration_seconds, is_demo)
SELECT m.id, 'Writing Your First SELECT', 'VIDEO',
       'https://cdn.example.com/demos/sql/select-intro.mp4', 360, 1
FROM modules m
         JOIN courses c ON c.id = m.course_id
WHERE c.slug = 'sql-fundamentals' AND m.title = 'SQL Basics'
  AND NOT EXISTS (SELECT 1 FROM lessons l WHERE l.module_id = m.id AND l.title = 'Writing Your First SELECT');


/* =========================
   Machine Learning Intro
   ========================= */
INSERT INTO modules (course_id, title, position, description)
SELECT c.id, 'ML Foundations', 1,
       'Supervised learning, metrics, and model intuition.'
FROM courses c
WHERE c.slug = 'machine-learning-intro'
  AND NOT EXISTS (
    SELECT 1 FROM modules m WHERE m.course_id = c.id AND m.title = 'ML Foundations'
);

INSERT INTO lessons (module_id, title, type, content_url, duration_seconds, is_demo)
SELECT m.id, 'Train/Test Split Explained', 'VIDEO',
       'https://cdn.example.com/demos/ml/train-test-split.mp4', 420, 1
FROM modules m
         JOIN courses c ON c.id = m.course_id
WHERE c.slug = 'machine-learning-intro' AND m.title = 'ML Foundations'
  AND NOT EXISTS (SELECT 1 FROM lessons l WHERE l.module_id = m.id AND l.title = 'Train/Test Split Explained');


/* =========================
   Cybersecurity Basics
   ========================= */
INSERT INTO modules (course_id, title, position, description)
SELECT c.id, 'Security Essentials', 1,
       'Threats, attacks, and defense strategies for modern systems.'
FROM courses c
WHERE c.slug = 'cybersecurity-basics'
  AND NOT EXISTS (
    SELECT 1 FROM modules m WHERE m.course_id = c.id AND m.title = 'Security Essentials'
);

INSERT INTO lessons (module_id, title, type, content_url, duration_seconds, is_demo)
SELECT m.id, 'Common Attack Types', 'VIDEO',
       'https://cdn.example.com/demos/sec/attack-types.mp4', 360, 1
FROM modules m
         JOIN courses c ON c.id = m.course_id
WHERE c.slug = 'cybersecurity-basics' AND m.title = 'Security Essentials'
  AND NOT EXISTS (SELECT 1 FROM lessons l WHERE l.module_id = m.id AND l.title = 'Common Attack Types');


/* =========================
   Flutter Mobile Apps
   ========================= */
INSERT INTO modules (course_id, title, position, description)
SELECT c.id, 'Flutter Basics', 1,
       'Widgets, layouts, and navigation for cross-platform apps.'
FROM courses c
WHERE c.slug = 'flutter-mobile-apps'
  AND NOT EXISTS (
    SELECT 1 FROM modules m WHERE m.course_id = c.id AND m.title = 'Flutter Basics'
);

INSERT INTO lessons (module_id, title, type, content_url, duration_seconds, is_demo)
SELECT m.id, 'Building Your First Widget', 'VIDEO',
       'https://cdn.example.com/demos/flutter/first-widget.mp4', 300, 1
FROM modules m
         JOIN courses c ON c.id = m.course_id
WHERE c.slug = 'flutter-mobile-apps' AND m.title = 'Flutter Basics'
  AND NOT EXISTS (SELECT 1 FROM lessons l WHERE l.module_id = m.id AND l.title = 'Building Your First Widget');


/* =========================
   UX Writing
   ========================= */
INSERT INTO modules (course_id, title, position, description)
SELECT c.id, 'Microcopy Essentials', 1,
       'Voice, tone, and clarity for product interfaces.'
FROM courses c
WHERE c.slug = 'ux-writing'
  AND NOT EXISTS (
    SELECT 1 FROM modules m WHERE m.course_id = c.id AND m.title = 'Microcopy Essentials'
);

INSERT INTO lessons (module_id, title, type, content_url, duration_seconds, is_demo)
SELECT m.id, 'Writing Error Messages', 'TEXT',
       'https://cdn.example.com/notes/ux/error-messages.html', 240, 1
FROM modules m
         JOIN courses c ON c.id = m.course_id
WHERE c.slug = 'ux-writing' AND m.title = 'Microcopy Essentials'
  AND NOT EXISTS (SELECT 1 FROM lessons l WHERE l.module_id = m.id AND l.title = 'Writing Error Messages');


/* =========================
   Public Speaking for Tech
   ========================= */
INSERT INTO modules (course_id, title, position, description)
SELECT c.id, 'Presentation Skills', 1,
       'Structure, storytelling, and Q&A for technical talks.'
FROM courses c
WHERE c.slug = 'public-speaking-for-tech'
  AND NOT EXISTS (
    SELECT 1 FROM modules m WHERE m.course_id = c.id AND m.title = 'Presentation Skills'
);

INSERT INTO lessons (module_id, title, type, content_url, duration_seconds, is_demo)
SELECT m.id, 'Handling Q&A', 'VIDEO',
       'https://cdn.example.com/demos/speak/handling-qa.mp4', 360, 1
FROM modules m
         JOIN courses c ON c.id = m.course_id
WHERE c.slug = 'public-speaking-for-tech' AND m.title = 'Presentation Skills'
  AND NOT EXISTS (SELECT 1 FROM lessons l WHERE l.module_id = m.id AND l.title = 'Handling Q&A');


/* =========================
   Financial Accounting
   ========================= */
INSERT INTO modules (course_id, title, position, description)
SELECT c.id, 'Accounting Basics', 1,
       'Balance sheets, income statements, and cash flows.'
FROM courses c
WHERE c.slug = 'financial-accounting'
  AND NOT EXISTS (
    SELECT 1 FROM modules m WHERE m.course_id = c.id AND m.title = 'Accounting Basics'
);

INSERT INTO lessons (module_id, title, type, content_url, duration_seconds, is_demo)
SELECT m.id, 'Reading a Balance Sheet', 'TEXT',
       'https://cdn.example.com/notes/fin/balance-sheet.html', 300, 1
FROM modules m
         JOIN courses c ON c.id = m.course_id
WHERE c.slug = 'financial-accounting' AND m.title = 'Accounting Basics'
  AND NOT EXISTS (SELECT 1 FROM lessons l WHERE l.module_id = m.id AND l.title = 'Reading a Balance Sheet');


/* =========================
   Data Visualization with Tableau
   ========================= */
INSERT INTO modules (course_id, title, position, description)
SELECT c.id, 'Tableau Essentials', 1,
       'Charts, dashboards, and storytelling with data.'
FROM courses c
WHERE c.slug = 'data-viz-with-tableau'
  AND NOT EXISTS (
    SELECT 1 FROM modules m WHERE m.course_id = c.id AND m.title = 'Tableau Essentials'
);

INSERT INTO lessons (module_id, title, type, content_url, duration_seconds, is_demo)
SELECT m.id, 'Building a Dashboard', 'VIDEO',
       'https://cdn.example.com/demos/tableau/dashboard.mp4', 420, 1
FROM modules m
         JOIN courses c ON c.id = m.course_id
WHERE c.slug = 'data-viz-with-tableau' AND m.title = 'Tableau Essentials'
  AND NOT EXISTS (SELECT 1 FROM lessons l WHERE l.module_id = m.id AND l.title = 'Building a Dashboard');


/* =========================
   Advanced Generative AI
   ========================= */
INSERT INTO modules (course_id, title, position, description)
SELECT c.id, 'GenAI Fundamentals', 1,
       'Tokenization, embeddings, and prompt engineering.'
FROM courses c
WHERE c.slug = 'advanced-generative-ai'
  AND NOT EXISTS (
    SELECT 1 FROM modules m WHERE m.course_id = c.id AND m.title = 'GenAI Fundamentals'
);

INSERT INTO lessons (module_id, title, type, content_url, duration_seconds, is_demo)
SELECT m.id, 'Prompt Patterns', 'TEXT',
       'https://cdn.example.com/notes/genai/prompt-patterns.html', 360, 1
FROM modules m
         JOIN courses c ON c.id = m.course_id
WHERE c.slug = 'advanced-generative-ai' AND m.title = 'GenAI Fundamentals'
  AND NOT EXISTS (SELECT 1 FROM lessons l WHERE l.module_id = m.id AND l.title = 'Prompt Patterns');