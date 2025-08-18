-- Soft delete for users: add deleted_at column
ALTER TABLE users ADD COLUMN deleted_at TIMESTAMP NULL;

