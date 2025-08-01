CREATE SCHEMA IF NOT EXISTS notifications_schema;

CREATE TABLE IF NOT EXISTS notifications_schema.notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    message TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    read BOOLEAN DEFAULT FALSE
);
