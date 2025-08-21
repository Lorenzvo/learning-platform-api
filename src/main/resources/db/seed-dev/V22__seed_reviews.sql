-- Seed: Reviews (3 per course) for courses id 2..20 using users 2..21
-- Idempotent via WHERE NOT EXISTS (user_id, course_id, comment) pattern.

-- ===== Course 2 =====
INSERT INTO reviews (user_id, course_id, rating, comment, created_at)
SELECT 2, 2, 5, 'Excellent intro—clear structure and useful examples.', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE user_id=2 AND course_id=2 AND comment='Excellent intro—clear structure and useful examples.');

INSERT INTO reviews (user_id, course_id, rating, comment, created_at)
SELECT 3, 2, 4, 'Good pacing and practical takeaways.', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE user_id=3 AND course_id=2 AND comment='Good pacing and practical takeaways.');

INSERT INTO reviews (user_id, course_id, rating, comment, created_at)
SELECT 4, 2, 4, 'Learned a lot in a short time.', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE user_id=4 AND course_id=2 AND comment='Learned a lot in a short time.');

-- ===== Course 3 =====
INSERT INTO reviews (user_id, course_id, rating, comment, created_at)
SELECT 5, 3, 5, 'Super helpful—concise and on point.', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE user_id=5 AND course_id=3 AND comment='Super helpful—concise and on point.');

INSERT INTO reviews (user_id, course_id, rating, comment, created_at)
SELECT 6, 3, 4, 'Solid fundamentals with real-world tips.', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE user_id=6 AND course_id=3 AND comment='Solid fundamentals with real-world tips.');

INSERT INTO reviews (user_id, course_id, rating, comment, created_at)
SELECT 7, 3, 3, 'Decent overview—could use more depth.', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE user_id=7 AND course_id=3 AND comment='Decent overview—could use more depth.');

-- ===== Course 4 =====
INSERT INTO reviews (user_id, course_id, rating, comment, created_at)
SELECT 8, 4, 5, 'Great flow and well-explained concepts.', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE user_id=8 AND course_id=4 AND comment='Great flow and well-explained concepts.');

INSERT INTO reviews (user_id, course_id, rating, comment, created_at)
SELECT 9, 4, 4, 'Clear explanations and good examples.', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE user_id=9 AND course_id=4 AND comment='Clear explanations and good examples.');

INSERT INTO reviews (user_id, course_id, rating, comment, created_at)
SELECT 10, 4, 3, 'Helpful, but a bit fast in parts.', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE user_id=10 AND course_id=4 AND comment='Helpful, but a bit fast in parts.');

-- ===== Course 5 =====
INSERT INTO reviews (user_id, course_id, rating, comment, created_at)
SELECT 11, 5, 5, 'Loved the hands-on approach.', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE user_id=11 AND course_id=5 AND comment='Loved the hands-on approach.');

INSERT INTO reviews (user_id, course_id, rating, comment, created_at)
SELECT 12, 5, 4, 'Good coverage and practical demos.', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE user_id=12 AND course_id=5 AND comment='Good coverage and practical demos.');

INSERT INTO reviews (user_id, course_id, rating, comment, created_at)
SELECT 13, 5, 3, 'Useful overview; could add more exercises.', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE user_id=13 AND course_id=5 AND comment='Useful overview; could add more exercises.');

-- ===== Course 6 =====
INSERT INTO reviews (user_id, course_id, rating, comment, created_at)
SELECT 14, 6, 5, 'Crystal clear—made tough topics easier.', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE user_id=14 AND course_id=6 AND comment='Crystal clear—made tough topics easier.');

INSERT INTO reviews (user_id, course_id, rating, comment, created_at)
SELECT 15, 6, 4, 'Strong examples and structure.', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE user_id=15 AND course_id=6 AND comment='Strong examples and structure.');

INSERT INTO reviews (user_id, course_id, rating, comment, created_at)
SELECT 16, 6, 2, 'Some sections felt rushed.', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE user_id=16 AND course_id=6 AND comment='Some sections felt rushed.');

-- ===== Course 7 =====
INSERT INTO reviews (user_id, course_id, rating, comment, created_at)
SELECT 17, 7, 5, 'Excellent balance of theory and practice.', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE user_id=17 AND course_id=7 AND comment='Excellent balance of theory and practice.');

INSERT INTO reviews (user_id, course_id, rating, comment, created_at)
SELECT 18, 7, 4, 'Very practical—loved the project.', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE user_id=18 AND course_id=7 AND comment='Very practical—loved the project.');

INSERT INTO reviews (user_id, course_id, rating, comment, created_at)
SELECT 19, 7, 3, 'Good content, minor gaps in detail.', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE user_id=19 AND course_id=7 AND comment='Good content, minor gaps in detail.');

-- ===== Course 8 =====
INSERT INTO reviews (user_id, course_id, rating, comment, created_at)
SELECT 20, 9, 5, 'Great explanations—easy to follow.', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE user_id=20 AND course_id=9 AND comment='Great explanations—easy to follow.');

INSERT INTO reviews (user_id, course_id, rating, comment, created_at)
SELECT 21, 9, 4, 'Solid walkthroughs and tips.', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE user_id=21 AND course_id=9 AND comment='Solid walkthroughs and tips.');

INSERT INTO reviews (user_id, course_id, rating, comment, created_at)
SELECT 2, 9, 3, 'Helpful starter course.', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE user_id=2 AND course_id=9 AND comment='Helpful starter course.');

-- ===== Course 9 =====
INSERT INTO reviews (user_id, course_id, rating, comment, created_at)
SELECT 3, 10, 5, 'Exceptional clarity and structure.', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE user_id=3 AND course_id=10 AND comment='Exceptional clarity and structure.');

INSERT INTO reviews (user_id, course_id, rating, comment, created_at)
SELECT 4, 10, 4, 'Good examples; learned a ton.', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE user_id=4 AND course_id=10 AND comment='Good examples; learned a ton.');

INSERT INTO reviews (user_id, course_id, rating, comment, created_at)
SELECT 5, 10, 2, 'Some topics needed more depth.', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE user_id=5 AND course_id=10 AND comment='Some topics needed more depth.');

-- ===== Course 10 =====
INSERT INTO reviews (user_id, course_id, rating, comment, created_at)
SELECT 6, 11, 5, 'Engaging and very practical.', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE user_id=6 AND course_id=11 AND comment='Engaging and very practical.');

INSERT INTO reviews (user_id, course_id, rating, comment, created_at)
SELECT 7, 11, 4, 'Well-paced and informative.', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE user_id=7 AND course_id=11 AND comment='Well-paced and informative.');

INSERT INTO reviews (user_id, course_id, rating, comment, created_at)
SELECT 8, 11, 3, 'Good overview; a few gaps.', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE user_id=8 AND course_id=11 AND comment='Good overview; a few gaps.');

-- ===== Course 11 =====
INSERT INTO reviews (user_id, course_id, rating, comment, created_at)
SELECT 9, 12, 5, 'Super clear—highly recommend.', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE user_id=9 AND course_id=12 AND comment='Super clear—highly recommend.');

INSERT INTO reviews (user_id, course_id, rating, comment, created_at)
SELECT 10, 12, 4, 'Solid content and delivery.', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE user_id=10 AND course_id=12 AND comment='Solid content and delivery.');

INSERT INTO reviews (user_id, course_id, rating, comment, created_at)
SELECT 11, 12, 2, 'Useful, but could be deeper.', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE user_id=11 AND course_id=12 AND comment='Useful, but could be deeper.');

-- ===== Course 12 =====
INSERT INTO reviews (user_id, course_id, rating, comment, created_at)
SELECT 12, 13, 5, 'Loved the practical exercises.', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE user_id=12 AND course_id=13 AND comment='Loved the practical exercises.');

INSERT INTO reviews (user_id, course_id, rating, comment, created_at)
SELECT 13, 13, 4, 'Good coverage and examples.', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE user_id=13 AND course_id=13 AND comment='Good coverage and examples.');

INSERT INTO reviews (user_id, course_id, rating, comment, created_at)
SELECT 14, 13, 3, 'Solid intro; some assumptions.', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE user_id=14 AND course_id=13 AND comment='Solid intro; some assumptions.');

-- ===== Course 13 =====
INSERT INTO reviews (user_id, course_id, rating, comment, created_at)
SELECT 15, 14, 5, 'Fantastic—clear and concise.', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE user_id=15 AND course_id=14 AND comment='Fantastic—clear and concise.');

INSERT INTO reviews (user_id, course_id, rating, comment, created_at)
SELECT 16, 14, 4, 'Very helpful explanations.', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE user_id=16 AND course_id=14 AND comment='Very helpful explanations.');

INSERT INTO reviews (user_id, course_id, rating, comment, created_at)
SELECT 17, 14, 2, 'A bit advanced in places.', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE user_id=17 AND course_id=14 AND comment='A bit advanced in places.');

-- ===== Course 14 =====
INSERT INTO reviews (user_id, course_id, rating, comment, created_at)
SELECT 18, 15, 5, 'Great structure and delivery.', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE user_id=18 AND course_id=15 AND comment='Great structure and delivery.');

INSERT INTO reviews (user_id, course_id, rating, comment, created_at)
SELECT 19, 15, 4, 'Well explained with good pace.', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE user_id=19 AND course_id=15 AND comment='Well explained with good pace.');

INSERT INTO reviews (user_id, course_id, rating, comment, created_at)
SELECT 20, 15, 3, 'Decent, could add more examples.', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE user_id=20 AND course_id=15 AND comment='Decent, could add more examples.');

-- ===== Course 15 =====
INSERT INTO reviews (user_id, course_id, rating, comment, created_at)
SELECT 21, 16, 5, 'Loved the clarity and demos.', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE user_id=21 AND course_id=16 AND comment='Loved the clarity and demos.');

INSERT INTO reviews (user_id, course_id, rating, comment, created_at)
SELECT 2, 16, 4, 'Informative and engaging.', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE user_id=2 AND course_id=16 AND comment='Informative and engaging.');

INSERT INTO reviews (user_id, course_id, rating, comment, created_at)
SELECT 3, 16, 2, 'Some parts felt too brief.', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE user_id=3 AND course_id=16 AND comment='Some parts felt too brief.');

-- ===== Course 16 =====
INSERT INTO reviews (user_id, course_id, rating, comment, created_at)
SELECT 4, 17, 5, 'Excellent instructor—very clear.', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE user_id=4 AND course_id=17 AND comment='Excellent instructor—very clear.');

INSERT INTO reviews (user_id, course_id, rating, comment, created_at)
SELECT 5, 17, 4, 'Good pace and structure.', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE user_id=5 AND course_id=17 AND comment='Good pace and structure.');

INSERT INTO reviews (user_id, course_id, rating, comment, created_at)
SELECT 6, 17, 3, 'Helpful but could go deeper.', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE user_id=6 AND course_id=17 AND comment='Helpful but could go deeper.');

-- ===== Course 17 =====
INSERT INTO reviews (user_id, course_id, rating, comment, created_at)
SELECT 7, 18, 5, 'Very practical—learned a lot.', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE user_id=7 AND course_id=18 AND comment='Very practical—learned a lot.');

INSERT INTO reviews (user_id, course_id, rating, comment, created_at)
SELECT 8, 18, 4, 'Clear and useful examples.', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE user_id=8 AND course_id=18 AND comment='Clear and useful examples.');

INSERT INTO reviews (user_id, course_id, rating, comment, created_at)
SELECT 9, 18, 2, 'Challenging in spots.', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE user_id=9 AND course_id=18 AND comment='Challenging in spots.');

-- ===== Course 18 =====
INSERT INTO reviews (user_id, course_id, rating, comment, created_at)
SELECT 10, 19, 5, 'Top-notch explanations.', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE user_id=10 AND course_id=19 AND comment='Top-notch explanations.');

INSERT INTO reviews (user_id, course_id, rating, comment, created_at)
SELECT 11, 19, 4, 'Good breadth and depth.', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE user_id=11 AND course_id=19 AND comment='Good breadth and depth.');

INSERT INTO reviews (user_id, course_id, rating, comment, created_at)
SELECT 12, 19, 3, 'Decent, a few gaps.', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE user_id=12 AND course_id=19 AND comment='Decent, a few gaps.');

-- ===== Course 19 =====
INSERT INTO reviews (user_id, course_id, rating, comment, created_at)
SELECT 13, 20, 5, 'Great course—highly recommend.', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE user_id=13 AND course_id=20 AND comment='Great course—highly recommend.');

INSERT INTO reviews (user_id, course_id, rating, comment, created_at)
SELECT 14, 20, 4, 'Well-structured and clear.', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE user_id=14 AND course_id=20 AND comment='Well-structured and clear.');

INSERT INTO reviews (user_id, course_id, rating, comment, created_at)
SELECT 15, 20, 2, 'Some areas were too advanced.', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE user_id=15 AND course_id=20 AND comment='Some areas were too advanced.');

-- ===== Course 20 =====
INSERT INTO reviews (user_id, course_id, rating, comment, created_at)
SELECT 16, 21, 5, 'Clear, concise, and practical.', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE user_id=16 AND course_id=21 AND comment='Clear, concise, and practical.');

INSERT INTO reviews (user_id, course_id, rating, comment, created_at)
SELECT 17, 21, 4, 'Good explanations and pace.', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE user_id=17 AND course_id=21 AND comment='Good explanations and pace.');

INSERT INTO reviews (user_id, course_id, rating, comment, created_at)
SELECT 18, 21, 3, 'Helpful intro overall.', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE user_id=18 AND course_id=21 AND comment='Helpful intro overall.');