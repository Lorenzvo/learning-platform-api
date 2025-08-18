-- Add refunded_at to payments and revoked_at to enrollments
ALTER TABLE payments ADD COLUMN refunded_at TIMESTAMP NULL;
ALTER TABLE enrollments ADD COLUMN revoked_at TIMESTAMP NULL;

