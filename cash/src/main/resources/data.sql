INSERT INTO cash_schema.accounts (user_id, username, currency, name, balance, active, created_at, updated_at)
SELECT 1, 'user', 'RUB', 'Основной счет', 50000.00, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM cash_schema.accounts WHERE user_id = 1 AND username = 'user' AND currency = 'RUB');

INSERT INTO cash_schema.accounts (user_id, username, currency, name, balance, active, created_at, updated_at) 
SELECT 1, 'user', 'USD', 'Долларовый счет', 1500.00, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM cash_schema.accounts WHERE user_id = 1 AND username = 'user' AND currency = 'USD');

INSERT INTO cash_schema.accounts (user_id, username, currency, name, balance, active, created_at, updated_at) 
SELECT 1, 'user', 'EUR', 'Евро счет', 1200.00, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM cash_schema.accounts WHERE user_id = 1 AND username = 'user' AND currency = 'EUR');

INSERT INTO cash_schema.accounts (user_id, username, currency, name, balance, active, created_at, updated_at) 
SELECT 2, 'user2', 'RUB', 'Основной счет', 100000.00, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM cash_schema.accounts WHERE user_id = 2 AND username = 'user2' AND currency = 'RUB');

INSERT INTO cash_schema.accounts (user_id, username, currency, name, balance, active, created_at, updated_at) 
SELECT 2, 'user2', 'USD', 'Долларовый счет', 2000.00, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM cash_schema.accounts WHERE user_id = 2 AND username = 'user2' AND currency = 'USD');

INSERT INTO cash_schema.accounts (user_id, username, currency, name, balance, active, created_at, updated_at) 
SELECT 2, 'user2', 'EUR', 'Евро счет', 1500.00, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM cash_schema.accounts WHERE user_id = 2 AND username = 'user2' AND currency = 'EUR');
