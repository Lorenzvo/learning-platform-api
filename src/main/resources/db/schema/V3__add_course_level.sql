-- V3: Add 'level' column to courses table for course difficulty/target audience
ALTER TABLE courses ADD COLUMN level VARCHAR(50) NULL AFTER description;
-- Inline comment: This migration adds the 'level' column to support filtering and display in the API.
-- Safe to run: does not affect existing data, allows nulls for legacy rows.

