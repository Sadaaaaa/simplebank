-- user (id=1)
INSERT INTO users (
    id, username, password, email, first_name, last_name, roles, enabled, birth_date, created_at, updated_at, deleted_at, deleted_by
) VALUES (
             1, 'user', '$2a$12$.DHb5YQCuwroi2.8s/0yi.CsQMXWeVy/cgbjYzzm1l3WIatey.h6S', 'user@example.com', 'Тестовый', 'Пользователь', 'USER', true, '1990-01-01', now(), now(), null, null
         ) ON CONFLICT (id) DO NOTHING;

-- user2 (id=2)
INSERT INTO users (
    id, username, password, email, first_name, last_name, roles, enabled, birth_date, created_at, updated_at, deleted_at, deleted_by
) VALUES (
             2, 'user2', '$2a$12$.DHb5YQCuwroi2.8s/0yi.CsQMXWeVy/cgbjYzzm1l3WIatey.h6S', 'user2@example.com', 'Второй', 'Пользователь', 'USER', true, '1992-02-02', now(), now(), null, null
         ) ON CONFLICT (id) DO NOTHING;
-- Пароль: password (bcrypt, твой рабочий хеш)