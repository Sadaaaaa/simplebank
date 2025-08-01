CREATE SCHEMA IF NOT EXISTS transfer_schema;

CREATE TABLE IF NOT EXISTS transfer_schema.transfer_history (
    id BIGSERIAL PRIMARY KEY,
    from_user_id BIGINT,
    to_user_id BIGINT,
    from_account_id BIGINT,
    to_account_id BIGINT,
    from_currency VARCHAR(10),
    to_currency VARCHAR(10),
    amount_from DECIMAL(19,2),
    amount_to DECIMAL(19,2),
    rate DECIMAL(19,6),
    date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    allowed BOOLEAN DEFAULT FALSE,
    block_reason VARCHAR(500),
    internal BOOLEAN DEFAULT FALSE
);
