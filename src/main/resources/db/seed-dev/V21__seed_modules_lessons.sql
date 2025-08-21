/* =========================
   Helpers by slug → course_id
   ========================= */

-- Digital Marketing
INSERT INTO modules (course_id, title, position, description)
SELECT c.id, 'Foundations of Digital Marketing', 1,
       'Core channels, funnels, and metrics. Understand how SEO, paid media, and content work together.'
FROM courses c
WHERE c.slug = 'digital-marketing'
  AND NOT EXISTS (
    SELECT 1 FROM modules m
    WHERE m.course_id = c.id AND m.title = 'Foundations of Digital Marketing'
);

INSERT INTO modules (course_id, title, position, description)
SELECT c.id, 'SEO & Content Strategy', 2,
       'On-page SEO, keyword research, and content calendars that support organic growth.'
FROM courses c
WHERE c.slug = 'digital-marketing'
  AND NOT EXISTS (
    SELECT 1 FROM modules m
    WHERE m.course_id = c.id AND m.title = 'SEO & Content Strategy'
);

-- Lessons for Digital Marketing
INSERT INTO lessons (module_id, title, type, content_url, duration_seconds, is_demo)
SELECT m.id, 'What is a Marketing Funnel?', 'VIDEO',
       'https://cdn.example.com/demos/dm/funnel-intro.mp4', 360, 1
FROM modules m
         JOIN courses c ON c.id = m.course_id
WHERE c.slug = 'digital-marketing' AND m.title = 'Foundations of Digital Marketing'
  AND NOT EXISTS (SELECT 1 FROM lessons l WHERE l.module_id = m.id AND l.title = 'What is a Marketing Funnel?');

INSERT INTO lessons (module_id, title, type, content_url, duration_seconds, is_demo)
SELECT m.id, 'Channel Overview (SEO, Paid, Email, Social)', 'TEXT',
       'https://cdn.example.com/notes/dm/channel-overview.html', 420, 0
FROM modules m
         JOIN courses c ON c.id = m.course_id
WHERE c.slug = 'digital-marketing' AND m.title = 'Foundations of Digital Marketing'
  AND NOT EXISTS (SELECT 1 FROM lessons l WHERE l.module_id = m.id AND l.title = 'Channel Overview (SEO, Paid, Email, Social)');

INSERT INTO lessons (module_id, title, type, content_url, duration_seconds, is_demo)
SELECT m.id, 'Keyword Research Basics', 'VIDEO',
       'https://cdn.example.com/demos/dm/keyword-research.mp4', 480, 0
FROM modules m
         JOIN courses c ON c.id = m.course_id
WHERE c.slug = 'digital-marketing' AND m.title = 'SEO & Content Strategy'
  AND NOT EXISTS (SELECT 1 FROM lessons l WHERE l.module_id = m.id AND l.title = 'Keyword Research Basics');


/* =========================
   Graphic Design
   ========================= */
INSERT INTO modules (course_id, title, position, description)
SELECT c.id, 'Design Principles', 1,
       'Hierarchy, contrast, alignment, proximity, and balance—foundations of visual design.'
FROM courses c
WHERE c.slug = 'graphic-design'
  AND NOT EXISTS (
    SELECT 1 FROM modules m WHERE m.course_id = c.id AND m.title = 'Design Principles'
);

INSERT INTO modules (course_id, title, position, description)
SELECT c.id, 'Typography & Color', 2,
       'Type pairing, scale, rhythm, and color systems for accessible, readable interfaces.'
FROM courses c
WHERE c.slug = 'graphic-design'
  AND NOT EXISTS (
    SELECT 1 FROM modules m WHERE m.course_id = c.id AND m.title = 'Typography & Color'
);

INSERT INTO lessons (module_id, title, type, content_url, duration_seconds, is_demo)
SELECT m.id, 'What Makes a Layout Work?', 'VIDEO',
       'https://cdn.example.com/demos/gd/layout-intro.mp4', 300, 1
FROM modules m
         JOIN courses c ON c.id = m.course_id
WHERE c.slug = 'graphic-design' AND m.title = 'Design Principles'
  AND NOT EXISTS (SELECT 1 FROM lessons l WHERE l.module_id = m.id AND l.title = 'What Makes a Layout Work?');

INSERT INTO lessons (module_id, title, type, content_url, duration_seconds, is_demo)
SELECT m.id, 'Type Hierarchy in Practice', 'TEXT',
       'https://cdn.example.com/notes/gd/type-hierarchy.html', 240, 0
FROM modules m
         JOIN courses c ON c.id = m.course_id
WHERE c.slug = 'graphic-design' AND m.title = 'Typography & Color'
  AND NOT EXISTS (SELECT 1 FROM lessons l WHERE l.module_id = m.id AND l.title = 'Type Hierarchy in Practice');


/* =========================
   Linear Algebra
   ========================= */
INSERT INTO modules (course_id, title, position, description)
SELECT c.id, 'Vectors & Matrices', 1,
       'Geometric intuition for vectors, matrix operations, and linear transformations.'
FROM courses c
WHERE c.slug = 'linear-algebra'
  AND NOT EXISTS (
    SELECT 1 FROM modules m WHERE m.course_id = c.id AND m.title = 'Vectors & Matrices'
);

INSERT INTO modules (course_id, title, position, description)
SELECT c.id, 'Eigenvalues & Eigenvectors', 2,
       'Spectral intuition and applications in data science and graphics.'
FROM courses c
WHERE c.slug = 'linear-algebra'
  AND NOT EXISTS (
    SELECT 1 FROM modules m WHERE m.course_id = c.id AND m.title = 'Eigenvalues & Eigenvectors'
);

INSERT INTO lessons (module_id, title, type, content_url, duration_seconds, is_demo)
SELECT m.id, 'Linear Transformations as Geometry', 'VIDEO',
       'https://cdn.example.com/demos/la/linear-transformations.mp4', 420, 1
FROM modules m
         JOIN courses c ON c.id = m.course_id
WHERE c.slug = 'linear-algebra' AND m.title = 'Vectors & Matrices'
  AND NOT EXISTS (SELECT 1 FROM lessons l WHERE l.module_id = m.id AND l.title = 'Linear Transformations as Geometry');

INSERT INTO lessons (module_id, title, type, content_url, duration_seconds, is_demo)
SELECT m.id, 'Finding Eigenvalues (Worked Example)', 'TEXT',
       'https://cdn.example.com/notes/la/eigenvalues-example.html', 480, 0
FROM modules m
         JOIN courses c ON c.id = m.course_id
WHERE c.slug = 'linear-algebra' AND m.title = 'Eigenvalues & Eigenvectors'
  AND NOT EXISTS (SELECT 1 FROM lessons l WHERE l.module_id = m.id AND l.title = 'Finding Eigenvalues (Worked Example)');


/* =========================
   Cloud Computing
   ========================= */
INSERT INTO modules (course_id, title, position, description)
SELECT c.id, 'Cloud Fundamentals', 1,
       'Compute, storage, networking, IAM, and shared responsibility model.'
FROM courses c
WHERE c.slug = 'cloud-computing'
  AND NOT EXISTS (
    SELECT 1 FROM modules m WHERE m.course_id = c.id AND m.title = 'Cloud Fundamentals'
);

INSERT INTO modules (course_id, title, position, description)
SELECT c.id, 'Deploying a Web App', 2,
       'From container to cloud: images, registry, service, and monitoring.'
FROM courses c
WHERE c.slug = 'cloud-computing'
  AND NOT EXISTS (
    SELECT 1 FROM modules m WHERE m.course_id = c.id AND m.title = 'Deploying a Web App'
);

INSERT INTO lessons (module_id, title, type, content_url, duration_seconds, is_demo)
SELECT m.id, 'Regions, AZs, and Networking Basics', 'VIDEO',
       'https://cdn.example.com/demos/cloud/regions-azs.mp4', 360, 1
FROM modules m
         JOIN courses c ON c.id = m.course_id
WHERE c.slug = 'cloud-computing' AND m.title = 'Cloud Fundamentals'
  AND NOT EXISTS (SELECT 1 FROM lessons l WHERE l.module_id = m.id AND l.title = 'Regions, AZs, and Networking Basics');

INSERT INTO lessons (module_id, title, type, content_url, duration_seconds, is_demo)
SELECT m.id, 'Deploying a Containerized Service', 'TEXT',
       'https://cdn.example.com/notes/cloud/container-deploy.html', 420, 0
FROM modules m
         JOIN courses c ON c.id = m.course_id
WHERE c.slug = 'cloud-computing' AND m.title = 'Deploying a Web App'
  AND NOT EXISTS (SELECT 1 FROM lessons l WHERE l.module_id = m.id AND l.title = 'Deploying a Containerized Service');


/* =========================
   Python for Data Science
   ========================= */
INSERT INTO modules (course_id, title, position, description)
SELECT c.id, 'Python & Pandas', 1,
       'Set up Python, explore data frames, and shape data for analysis.'
FROM courses c
WHERE c.slug = 'python-for-data-science'
  AND NOT EXISTS (
    SELECT 1 FROM modules m WHERE m.course_id = c.id AND m.title = 'Python & Pandas'
);

INSERT INTO modules (course_id, title, position, description)
SELECT c.id, 'Visualization Basics', 2,
       'Quick plots for insights with Matplotlib/Altair.'
FROM courses c
WHERE c.slug = 'python-for-data-science'
  AND NOT EXISTS (
    SELECT 1 FROM modules m WHERE m.course_id = c.id AND m.title = 'Visualization Basics'
);

INSERT INTO lessons (module_id, title, type, content_url, duration_seconds, is_demo)
SELECT m.id, 'Getting Started with pandas', 'VIDEO',
       'https://cdn.example.com/demos/pyds/pandas-intro.mp4', 420, 1
FROM modules m
         JOIN courses c ON c.id = m.course_id
WHERE c.slug = 'python-for-data-science' AND m.title = 'Python & Pandas'
  AND NOT EXISTS (SELECT 1 FROM lessons l WHERE l.module_id = m.id AND l.title = 'Getting Started with pandas');

INSERT INTO lessons (module_id, title, type, content_url, duration_seconds, is_demo)
SELECT m.id, 'Plotting First Insights', 'TEXT',
       'https://cdn.example.com/notes/pyds/first-plots.html', 300, 0
FROM modules m
         JOIN courses c ON c.id = m.course_id
WHERE c.slug = 'python-for-data-science' AND m.title = 'Visualization Basics'
  AND NOT EXISTS (SELECT 1 FROM lessons l WHERE l.module_id = m.id AND l.title = 'Plotting First Insights');


/* =========================
   DevOps Foundations
   ========================= */
INSERT INTO modules (course_id, title, position, description)
SELECT c.id, 'CI/CD Pipeline Basics', 1,
       'Pipelines, triggers, and artifact flow from commit to deploy.'
FROM courses c
WHERE c.slug = 'devops-foundations'
  AND NOT EXISTS (
    SELECT 1 FROM modules m WHERE m.course_id = c.id AND m.title = 'CI/CD Pipeline Basics'
);

INSERT INTO modules (course_id, title, position, description)
SELECT c.id, 'Infrastructure as Code', 2,
       'Declarative infra, drift, and reviewable changes.'
FROM courses c
WHERE c.slug = 'devops-foundations'
  AND NOT EXISTS (
    SELECT 1 FROM modules m WHERE m.course_id = c.id AND m.title = 'Infrastructure as Code'
);

INSERT INTO lessons (module_id, title, type, content_url, duration_seconds, is_demo)
SELECT m.id, 'From Commit to Deploy (Demo)', 'VIDEO',
       'https://cdn.example.com/demos/devops/commit-to-deploy.mp4', 360, 1
FROM modules m
         JOIN courses c ON c.id = m.course_id
WHERE c.slug = 'devops-foundations' AND m.title = 'CI/CD Pipeline Basics'
  AND NOT EXISTS (SELECT 1 FROM lessons l WHERE l.module_id = m.id AND l.title = 'From Commit to Deploy (Demo)');

INSERT INTO lessons (module_id, title, type, content_url, duration_seconds, is_demo)
SELECT m.id, 'IaC in Practice', 'TEXT',
       'https://cdn.example.com/notes/devops/iac-practice.html', 420, 0
FROM modules m
         JOIN courses c ON c.id = m.course_id
WHERE c.slug = 'devops-foundations' AND m.title = 'Infrastructure as Code'
  AND NOT EXISTS (SELECT 1 FROM lessons l WHERE l.module_id = m.id AND l.title = 'IaC in Practice');
