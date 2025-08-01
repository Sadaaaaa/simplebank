CREATE SCHEMA IF NOT EXISTS exchange_schema;

CREATE TABLE IF NOT EXISTS exchange_schema.currency_exchange_facts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    from_currency VARCHAR(10),
    to_currency VARCHAR(10),
    amount_from DECIMAL(19,2),
    amount_to DECIMAL(19,2),
    rate DECIMAL(19,6),
    date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    internal BOOLEAN DEFAULT FALSE
);
