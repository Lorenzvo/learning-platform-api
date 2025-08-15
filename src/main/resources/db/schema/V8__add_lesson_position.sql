-- Add position column to lessons for ordering within modules
ALTER TABLE lessons ADD COLUMN position INT NOT NULL DEFAULT 0;
-- If lessons already exist, you may want to update their position values in a follow-up migration.

