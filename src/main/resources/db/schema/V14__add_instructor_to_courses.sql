-- Add instructor_id column to courses table
ALTER TABLE courses
ADD COLUMN instructor_id BIGINT,
ADD CONSTRAINT fk_courses_instructor FOREIGN KEY (instructor_id) REFERENCES instructors(id);

