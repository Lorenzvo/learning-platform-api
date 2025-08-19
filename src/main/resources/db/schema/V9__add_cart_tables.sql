-- V9__add_cart_tables.sql
-- Add carts and cart_items tables for multi-course cart functionality

-- Carts: one per user, created at first use
CREATE TABLE carts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE, -- Each user can have only one cart
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Cart items: each row is a course in a user's cart
CREATE TABLE cart_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    cart_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cartitem_cart FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE,
    CONSTRAINT fk_cartitem_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE RESTRICT,
    CONSTRAINT uq_cartitem UNIQUE (cart_id, course_id) -- Only one of each course per cart
);

-- Indexes for efficient lookup
CREATE INDEX idx_cartitem_cart_id ON cart_items(cart_id);
CREATE INDEX idx_cartitem_course_id ON cart_items(course_id);

-- Note: Quantity is not modeled because courses are non-stackable (one enrollment per course per user).
-- Uniqueness constraint ensures a course can only appear once per cart.
-- ON DELETE CASCADE for cart_id: deleting a cart removes its items; ON DELETE RESTRICT for course_id: cannot remove a course if in any cart.
