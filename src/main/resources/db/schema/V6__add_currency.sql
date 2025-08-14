-- Add a new column 'currency' to the 'courses' table
-- to store the ISO 4217 currency code for the course price.
-- Default to 'USD' for backward compatibility.
ALTER TABLE courses ADD COLUMN currency CHAR(3) NOT NULL DEFAULT 'USD' AFTER price_cents;

-- Guardrail (MySQL 8.0.16+ enforces CHECK)
ALTER TABLE courses ADD CONSTRAINT ck_courses_currency CHECK (currency REGEXP '^[A-Z]{3}$');