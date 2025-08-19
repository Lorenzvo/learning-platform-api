-- Migration: Add payment_items table
CREATE TABLE payment_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    payment_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    amount_cents INT NOT NULL,
    currency CHAR(3) NOT NULL,
    CONSTRAINT fk_payment_items_payment FOREIGN KEY (payment_id) REFERENCES payments(id) ON DELETE CASCADE,
    CONSTRAINT fk_payment_items_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
);
-- Indexes for performance
CREATE INDEX idx_payment_items_payment_id ON payment_items(payment_id);
CREATE INDEX idx_payment_items_course_id ON payment_items(course_id);
