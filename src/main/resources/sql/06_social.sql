-- =====================================================================
-- 06 · Red social: seguimientos, favoritos y quedadas
-- =====================================================================

INSERT INTO follows (follower_id, followed_id, created_at)
SELECT f.id, t.id, NOW() - make_interval(days => v.days_ago)
FROM (VALUES
    ('adrianql5',       'maria_tapas',     40),
    ('adrianql5',       'xoan_compos',     38),
    ('adrianql5',       'hugoocoto',       30),
    ('maria_tapas',     'adrianql5',       39),
    ('xoan_compos',     'adrianql5',       35),
    ('laura_peregrina', 'maria_tapas',     18),
    ('maria_tapas',     'laura_peregrina', 17)
) AS v (follower, followed, days_ago)
JOIN users f ON f.username = v.follower
JOIN users t ON t.username = v.followed;

INSERT INTO favorites (user_id, business_id, created_at)
SELECT u.id, b.id, NOW() - make_interval(days => v.days_ago)
FROM (VALUES
    ('adrianql5',       'Abastos 2.0',         9),
    ('adrianql5',       'Petiscos do Cardeal', 8),
    ('maria_tapas',     'Bar Orella',         12),
    ('laura_peregrina', 'O Gato Negro',        4)
) AS v (username, business, days_ago)
JOIN users u ON u.username = v.username
JOIN businesses b ON b.name = v.business;

-- Quedadas: una próxima (de adrianql5) y una pasada (de maria_tapas)
INSERT INTO meetups (creator_id, title, description, scheduled_at, created_at)
SELECT u.id, v.title, v.description,
       date_trunc('day', NOW()) + make_interval(days => v.days_offset, hours => v.hour),
       NOW() - make_interval(days => v.created_days_ago)
FROM (VALUES
    ('adrianql5',   'Ruta pola Raíña',     'Tres clásicos de la Rúa da Raíña, de vino en vino.',   5, 13, 2),
    ('maria_tapas', 'Tarde no Ensanche',   'Caldo y bocadillo de zorza lejos de los turistas.',  -7, 19, 14)
) AS v (username, title, description, days_offset, hour, created_days_ago)
JOIN users u ON u.username = v.username;

INSERT INTO meetup_stops (meetup_id, business_id, stop_order)
SELECT m.id, b.id, v.stop_order
FROM (VALUES
    ('Ruta pola Raíña',   'Bar Orella',            1),
    ('Ruta pola Raíña',   'Viñoteca Ventosela',    2),
    ('Ruta pola Raíña',   'O Gato Negro',          3),
    ('Tarde no Ensanche', 'A Taberna do Ensanche', 1),
    ('Tarde no Ensanche', 'Raíces Galegas',        2)
) AS v (meetup, business, stop_order)
JOIN meetups m ON m.title = v.meetup
JOIN businesses b ON b.name = v.business;

-- Participantes (el creador no se incluye: se entiende que asiste)
INSERT INTO meetup_participants (user_id, meetup_id, status, invited_at, responded_at)
SELECT u.id, m.id, v.status, m.created_at,
       CASE WHEN v.status = 'INVITED' THEN NULL ELSE m.created_at + INTERVAL '5 hours' END
FROM (VALUES
    ('Ruta pola Raíña',   'maria_tapas',     'ACCEPTED'),
    ('Ruta pola Raíña',   'xoan_compos',     'INVITED'),
    ('Ruta pola Raíña',   'laura_peregrina', 'DECLINED'),
    ('Tarde no Ensanche', 'adrianql5',       'ACCEPTED'),
    ('Tarde no Ensanche', 'laura_peregrina', 'ACCEPTED')
) AS v (meetup, username, status)
JOIN meetups m ON m.title = v.meetup
JOIN users u ON u.username = v.username;

INSERT INTO meetup_comments (author_id, meetup_id, content, created_at)
SELECT u.id, m.id, v.content, m.created_at + make_interval(hours => v.hours_after)
FROM (VALUES
    ('Ruta pola Raíña',   'adrianql5',       '¿Quedamos en la puerta del Orella a la una?',   1),
    ('Ruta pola Raíña',   'maria_tapas',     '¡Perfecto! Allí estaré.',                        3),
    ('Ruta pola Raíña',   'laura_peregrina', 'Esta vez no puedo, ¡pasadlo bien!',              6),
    ('Tarde no Ensanche', 'maria_tapas',     'Reservé mesa en la Taberna para las siete.',    2),
    ('Tarde no Ensanche', 'adrianql5',       'Genial, llevo hambre de caldo.',                 4)
) AS v (meetup, username, content, hours_after)
JOIN meetups m ON m.title = v.meetup
JOIN users u ON u.username = v.username;
