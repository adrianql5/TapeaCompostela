-- =====================================================================
-- 01 · Usuarios
-- Todos tienen la contraseña "password" (hash BCrypt de ejemplo de la
-- documentación de Spring Security). Solo para desarrollo.
-- =====================================================================

INSERT INTO users (username, name, email, password, role, bio, created_at) VALUES
('adrianql5',       'Adrián',  'adrianql5@example.com',       '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'USER',  'Buscando la mejor tapa de Compostela.',        NOW() - INTERVAL '60 days'),
('hugoocoto',       'Hugo',    'hugoocoto@example.com',       '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'OWNER', 'Hostelero en el casco histórico y el Ensanche.', NOW() - INTERVAL '90 days'),
('admin',           'Administrador', 'admin@example.com',     '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'ADMIN', NULL,                                           NOW() - INTERVAL '120 days'),
-- Usuarios de relleno para que la red social tenga actividad
('maria_tapas',     'María',   'maria_tapas@example.com',     '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'USER',  'Estudiante en Santiago. Fan del pulpo.',        NOW() - INTERVAL '45 days'),
('xoan_compos',     'Xoán',    'xoan_compos@example.com',     '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'USER',  'Compostelano de toda la vida.',                 NOW() - INTERVAL '40 days'),
('laura_peregrina', 'Laura',   'laura_peregrina@example.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'USER',  'Llegué por el Camino y me quedé por las tapas.', NOW() - INTERVAL '20 days');
