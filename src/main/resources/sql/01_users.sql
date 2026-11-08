-- Roles y usuarios. Cada usuario tiene la contraseña "password"
-- lo que hay insertado es el hash de ejemplo de la documentación de Spring Security
INSERT INTO roles (name) VALUES ('USER'), ('OWNER'), ('ADMIN');

INSERT INTO users (username, name, email, password, points, created_at) VALUES
('adrianql5',   'Adrián',        'adrianql5@example.com',   '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 50, NOW() - INTERVAL '60 days'),
('hugoocoto',   'Hugo',          'hugoocoto@example.com',   '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 0,  NOW() - INTERVAL '90 days'),
('admin',       'Administrador', 'admin@example.com',       '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 0,  NOW() - INTERVAL '120 days'),
('maria_tapas', 'María',         'maria_tapas@example.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 40, NOW() - INTERVAL '45 days'),
('xoan_compos', 'Xoán',          'xoan_compos@example.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 10, NOW() - INTERVAL '40 days');

-- Cada cuenta es un USER, el propietario de un negocio es también OWNER y el administrador a mayores ADMIN
INSERT INTO user_roles (user_id, role_name)
SELECT u.id, v.role
FROM (VALUES
    ('adrianql5',   'USER'),
    ('hugoocoto',   'USER'),
    ('hugoocoto',   'OWNER'),
    ('admin',       'USER'),
    ('admin',       'ADMIN'),
    ('maria_tapas', 'USER'),
    ('xoan_compos', 'USER')
) AS v (username, role)
JOIN users u ON u.username = v.username;