-- V5: Add thumbnail_url to courses (nullable for legacy compatibility)
-- Add short description for emails/previews

ALTER TABLE courses ADD COLUMN thumbnail_url VARCHAR(255) DEFAULT NULL AFTER level;
ALTER TABLE courses ADD COLUMN short_description VARCHAR(280) NULL AFTER title;