INSERT INTO accounts_schema.users (
    id, username, password, email, first_name, last_name, roles, enabled, birth_date, created_at, updated_at, deleted_at, deleted_by
) VALUES (
             1, 'user', '$2a$12$.DHb5YQCuwroi2.8s/0yi.CsQMXWeVy/cgbjYzzm1l3WIatey.h6S', 'user@example.com', 'Тестовый', 'Пользователь', 'USER', true, '1990-01-01', now(), now(), null, null
         ) ON CONFLICT (id) DO NOTHING;

INSERT INTO accounts_schema.users (
    id, username, password, email, first_name, last_name, roles, enabled, birth_date, created_at, updated_at, deleted_at, deleted_by
) VALUES (
             2, 'user2', '$2a$12$.DHb5YQCuwroi2.8s/0yi.CsQMXWeVy/cgbjYzzm1l3WIatey.h6S', 'user2@example.com', 'Второй', 'Пользователь', 'USER', true, '1992-02-02', now(), now(), null, null
         ) ON CONFLICT (id) DO NOTHING;
-- Пароль: password

SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));