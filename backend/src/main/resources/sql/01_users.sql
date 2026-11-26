-- Roles y usuarios. Cada usuario tiene la contraseña "password"
-- lo que hay insertado es el hash de ejemplo de la documentación de Spring Security
INSERT INTO roles (name) VALUES ('USER'), ('OWNER'), ('ADMIN');

INSERT INTO users (username, name, email, password, points, created_at) VALUES
('adrianql5',   'Adrián',        'adrianql5@example.com',   '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 200,NOW() - INTERVAL '60 days'),
('hugoocoto',   'Hugo',          'hugoocoto@example.com',   '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 0,  NOW() - INTERVAL '90 days'),
('admin',       'Administrador', 'admin@example.com',       '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 0,  NOW() - INTERVAL '120 days'),
('maria_tapas', 'María',         'maria_tapas@example.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 40, NOW() - INTERVAL '45 days'),
('xoan_compos', 'Xoán',          'xoan_compos@example.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 10, NOW() - INTERVAL '40 days'),
('lucia_sdc',     'Lucía',         'lucia_sdc@example.com',         '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 70 , NOW() - INTERVAL '50 days'),
('brais_rua',     'Brais',         'brais_rua@example.com',         '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 30 , NOW() - INTERVAL '38 days'),
('antia_galega',  'Antía',         'antia_galega@example.com',      '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 120, NOW() - INTERVAL '35 days'),
('pablo_franco',  'Pablo',         'pablo_franco@example.com',      '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 20 , NOW() - INTERVAL '30 days'),
('noa_compos',    'Noa',           'noa_compos@example.com',        '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 60 , NOW() - INTERVAL '22 days'),
('iago_tapas',    'Iago',          'iago_tapas@example.com',        '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 40 , NOW() - INTERVAL '15 days'),
('carmen_raina',  'Carmen',        'carmen_raina@example.com',      '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 0  , NOW() - INTERVAL '100 days'),
('manolo_abastos','Manolo',        'manolo_abastos@example.com',    '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 0  , NOW() - INTERVAL '95 days');

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
    ('xoan_compos', 'USER'),
    ('lucia_sdc',      'USER'),
    ('brais_rua',      'USER'),
    ('antia_galega',   'USER'),
    ('pablo_franco',   'USER'),
    ('noa_compos',     'USER'),
    ('iago_tapas',     'USER'),
    ('carmen_raina',   'USER'),
    ('carmen_raina',   'OWNER'),
    ('manolo_abastos', 'USER'),
    ('manolo_abastos', 'OWNER')
) AS v (username, role)
JOIN users u ON u.username = v.username;