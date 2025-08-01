CREATE SCHEMA IF NOT EXISTS cash_schema;

CREATE TABLE IF NOT EXISTS cash_schema.accounts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    username VARCHAR(255),
    currency VARCHAR(10),
    name VARCHAR(255),
    balance DECIMAL(19,2) DEFAULT 0.00,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255)
);

