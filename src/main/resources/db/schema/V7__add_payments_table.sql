CREATE TABLE payments (
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id        BIGINT       NOT NULL,                 -- FK; RESTRICT to keep audit (no cascade delete)
    course_id      BIGINT       NOT NULL,                 -- FK; same rationale as above
    gateway_txn_id VARCHAR(64)  NULL,                     -- gateway charge/payment id; UNIQUE allows multiple NULLs in MySQL
    amount_cents   INT          NOT NULL,                 -- store money in minor units (no decimals/floats)
    currency       CHAR(3)      NOT NULL,                 -- ISO 4217 currency code (e.g., 'USD', 'EUR')
    status         VARCHAR(16)  NOT NULL,                 -- app enum values (kept as text for flexibility)
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,   -- match other tables' timestamp style

    CONSTRAINT fk_payments_user   FOREIGN KEY (user_id)   REFERENCES users(id)   ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_payments_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT ck_payments_amount_nonneg CHECK (amount_cents >= 0),
    CONSTRAINT ck_payments_currency CHECK (currency REGEXP '^[A-Z]{3}$'),
    CONSTRAINT ck_payments_status   CHECK (status IN ('PENDING','SUCCESS','FAILED','REFUNDED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Uniqueness for idempotency (prevents double-recording the same gateway txn)
CREATE UNIQUE INDEX uk_payments_gateway_txn_id ON payments(gateway_txn_id);

-- Helpful query paths
CREATE INDEX idx_payments_user       ON payments(user_id, created_at);     -- user history
CREATE INDEX idx_payments_course     ON payments(course_id, created_at);   -- per-course reporting
CREATE INDEX idx_payments_created_at ON payments(created_at);              -- chronological scans