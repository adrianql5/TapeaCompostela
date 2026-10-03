-- =====================================================================
-- 07 · Gamificación: recompensas, puntos, canjes e insignias
-- Solo los locales adheridos (los de hugoocoto) dan puntos y recompensas.
-- Saldos resultantes:
--   adrianql5   en O Gato Negro:          55 ganados - 30 canjeados = 25
--   adrianql5   en A Taberna do Ensanche: 25
--   maria_tapas en O Gato Negro:          70 ganados - 30 (canje pendiente) = 40
--   laura_peregrina en A Taberna do Ensanche: 10
-- =====================================================================

INSERT INTO rewards (business_id, title, description, points_cost, active, start_date, end_date)
SELECT b.id, v.title, v.description, v.points_cost, v.active,
       CURRENT_DATE - v.start_days_ago,
       CASE WHEN v.end_days_ago IS NULL THEN NULL ELSE CURRENT_DATE - v.end_days_ago END
FROM (VALUES
    ('O Gato Negro',          'Taza de ribeiro gratis',          'Una taza de ribeiro de la casa',          30,  TRUE,  60, NULL),
    ('O Gato Negro',          'Pulpo á feira a mitad de precio', 'Ración de pulpo con un 50 % de descuento', 150, TRUE,  60, NULL),
    ('A Taberna do Ensanche', 'Caldo gallego gratis',            'Un caldo con cualquier consumición',      50,  TRUE,  45, NULL),
    ('A Taberna do Ensanche', 'Promoción de verano',             'Caducada: sirve para probar filtros',     40,  FALSE, 120, 30)
) AS v (business, title, description, points_cost, active, start_days_ago, end_days_ago)
JOIN businesses b ON b.name = v.business;

-- Puntos ganados: los registra el propietario (hugoocoto)
INSERT INTO point_transactions (user_id, business_id, points, type, reason, created_at, registered_by_id)
SELECT u.id, b.id, v.points, 'EARN', v.reason, NOW() - make_interval(days => v.days_ago),
       (SELECT id FROM users WHERE username = 'hugoocoto')
FROM (VALUES
    ('adrianql5',       'O Gato Negro',          20, 'Consumición en barra',    25),
    ('adrianql5',       'O Gato Negro',          15, 'Consumición en barra',    12),
    ('adrianql5',       'O Gato Negro',          20, 'Comida para dos',          4),
    ('adrianql5',       'A Taberna do Ensanche', 25, 'Cena',                     8),
    ('maria_tapas',     'O Gato Negro',          40, 'Comida de grupo',         24),
    ('maria_tapas',     'O Gato Negro',          30, 'Consumición en barra',     6),
    ('laura_peregrina', 'A Taberna do Ensanche', 10, 'Consumición en barra',     3)
) AS v (username, business, points, reason, days_ago)
JOIN users u ON u.username = v.username
JOIN businesses b ON b.name = v.business;

-- Canjes: uno ya usado (adrianql5) y uno pendiente (maria_tapas)
INSERT INTO redemptions (user_id, reward_id, code, status, redeemed_at, used_at)
SELECT u.id, rw.id, v.code, v.status,
       NOW() - make_interval(days => v.days_ago),
       CASE WHEN v.status = 'USED' THEN NOW() - make_interval(days => v.days_ago) + INTERVAL '10 minutes' END
FROM (VALUES
    ('adrianql5',   'Taza de ribeiro gratis', 'TPC-7K2M9Q', 'USED',    3),
    ('maria_tapas', 'Taza de ribeiro gratis', 'TPC-X4R8LD', 'PENDING', 1)
) AS v (username, reward, code, status, days_ago)
JOIN users u ON u.username = v.username
JOIN rewards rw ON rw.title = v.reward;

-- Cada canje descuenta sus puntos con un movimiento REDEEM ligado a él
INSERT INTO point_transactions (user_id, business_id, points, type, reason, created_at, redemption_id)
SELECT rd.user_id, rw.business_id, -rw.points_cost, 'REDEEM', 'Canje: ' || rw.title, rd.redeemed_at, rd.id
FROM redemptions rd
JOIN rewards rw ON rw.id = rd.reward_id;

-- Catálogo de insignias
INSERT INTO badges (code, name, description, condition_type, target_value) VALUES
('FIRST_REVIEW', 'Primera reseña',       'Escribiste tu primera reseña',        'REVIEW_COUNT',        1),
('REVIEWER_10',  'Crítico gastronómico', 'Escribiste 10 reseñas',               'REVIEW_COUNT',        10),
('EXPLORER_5',   'Explorador',           'Reseñaste 5 locales distintos',       'BUSINESSES_REVIEWED', 5),
('POINTS_50',    'Cliente fiel',         'Ganaste 50 puntos',                   'POINTS_EARNED',       50),
('POINTS_200',   'Habitual',             'Ganaste 200 puntos',                  'POINTS_EARNED',       200);

-- Insignias obtenidas (coherentes con los datos de 05 y de este script)
INSERT INTO user_badges (user_id, badge_id, earned_at)
SELECT u.id, bd.id, NOW() - make_interval(days => v.days_ago)
FROM (VALUES
    ('adrianql5',       'FIRST_REVIEW', 28),
    ('adrianql5',       'EXPLORER_5',    6),
    ('adrianql5',       'POINTS_50',     4),
    ('maria_tapas',     'FIRST_REVIEW', 25),
    ('maria_tapas',     'POINTS_50',     6),
    ('xoan_compos',     'FIRST_REVIEW', 30),
    ('laura_peregrina', 'FIRST_REVIEW', 12),
    ('hugoocoto',       'FIRST_REVIEW', 16)
) AS v (username, badge, days_ago)
JOIN users u ON u.username = v.username
JOIN badges bd ON bd.code = v.badge;
