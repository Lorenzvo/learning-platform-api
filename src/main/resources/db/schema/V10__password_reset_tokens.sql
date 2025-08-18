-- V10__password_reset_tokens.sql
-- Migration for password reset tokens
-- Security notes: tokens are single-use, expire after 15min, and are not guessable (32 bytes hex)

CREATE TABLE password_reset_tokens (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT 0,
    CONSTRAINT fk_password_reset_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_password_reset_user_id ON password_reset_tokens(user_id);
CREATE INDEX idx_password_reset_token ON password_reset_tokens(token);

-- Security: Only one valid token per user should be allowed in business logic, but DB allows multiple for audit/history.
-- Tokens are single-use and expire after 15 minutes for security.

