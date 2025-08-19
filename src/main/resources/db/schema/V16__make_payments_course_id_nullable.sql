-- Migration: Make course_id nullable in payments table for bulk cart checkout
ALTER TABLE payments MODIFY course_id BIGINT NULL;

