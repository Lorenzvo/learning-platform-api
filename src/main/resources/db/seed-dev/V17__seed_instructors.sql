-- Add name column to instructors table
ALTER TABLE instructors ADD COLUMN name VARCHAR(100);

-- Seed instructors
INSERT INTO instructors (user_id, name, bio, avatar_url) VALUES (22, 'John Smith', '', '');
INSERT INTO instructors (user_id, name, bio, avatar_url) VALUES (23, 'Mary Smith', '', '');

-- Associate instructors with courses
UPDATE courses SET instructor_id = 1 WHERE id = 2;
UPDATE courses SET instructor_id = 2 WHERE id = 3;

