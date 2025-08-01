-- Инициализация тестовых данных для accounts сервиса

-- Тестовый пользователь 1
INSERT INTO accounts_schema.users (
    username, password, email, first_name, last_name, roles, enabled, birth_date, created_at, updated_at, deleted_at, deleted_by
) VALUES (
    'user', '$2a$12$.DHb5YQCuwroi2.8s/0yi.CsQMXWeVy/cgbjYzzm1l3WIatey.h6S', 'user@example.com', 'Тестовый', 'Пользователь', 'USER', true, '1990-01-01', now(), now(), null, null
) ON CONFLICT (username) DO NOTHING;

-- Тестовый пользователь 2
INSERT INTO accounts_schema.users (
    username, password, email, first_name, last_name, roles, enabled, birth_date, created_at, updated_at, deleted_at, deleted_by
) VALUES (
    'user2', '$2a$12$.DHb5YQCuwroi2.8s/0yi.CsQMXWeVy/cgbjYzzm1l3WIatey.h6S', 'user2@example.com', 'Второй', 'Пользователь', 'USER', true, '1992-02-02', now(), now(), null, null
) ON CONFLICT (username) DO NOTHING;

-- Пароль для обоих пользователей: password

-- Обновляем последовательность ID
SELECT setval('accounts_schema.users_id_seq', (SELECT COALESCE(MAX(id), 0) + 1 FROM accounts_schema.users));