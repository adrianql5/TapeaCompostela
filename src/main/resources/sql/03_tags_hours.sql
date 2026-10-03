-- =====================================================================
-- 03 · Etiquetas y horarios
-- Los horarios de Abastos 2.0 son los publicados; el resto son
-- ORIENTATIVOS, para tener datos con los que probar el filtro
-- "abierto ahora". day_of_week: 1 = lunes ... 7 = domingo.
-- =====================================================================

INSERT INTO tags (name) VALUES
('Tapa gratis'), ('Pulpo'), ('Marisco'), ('Vinos'), ('Tortilla'),
('Tradicional'), ('Cocina de mercado'), ('Raciones'),
('Casco histórico'), ('Ensanche');

INSERT INTO business_tags (business_id, tag_id)
SELECT b.id, t.id
FROM (VALUES
    ('O Gato Negro',                'Tradicional'),
    ('O Gato Negro',                'Raciones'),
    ('O Gato Negro',                'Casco histórico'),
    ('A Taberna do Ensanche',       'Vinos'),
    ('A Taberna do Ensanche',       'Raciones'),
    ('A Taberna do Ensanche',       'Ensanche'),
    ('Abastos 2.0',                 'Cocina de mercado'),
    ('Abastos 2.0',                 'Marisco'),
    ('Abastos 2.0',                 'Casco histórico'),
    ('Bar Orella',                  'Tapa gratis'),
    ('Bar Orella',                  'Tradicional'),
    ('Bar Orella',                  'Casco histórico'),
    ('Viñoteca Ventosela',          'Tapa gratis'),
    ('Viñoteca Ventosela',          'Vinos'),
    ('Viñoteca Ventosela',          'Casco histórico'),
    ('Bar Trafalgar',               'Tapa gratis'),
    ('Bar Trafalgar',               'Marisco'),
    ('Bar Trafalgar',               'Casco histórico'),
    ('Taberna O Celme do Caracol',  'Tradicional'),
    ('Taberna O Celme do Caracol',  'Casco histórico'),
    ('A Taberna do Bispo',          'Vinos'),
    ('A Taberna do Bispo',          'Raciones'),
    ('A Taberna do Bispo',          'Casco histórico'),
    ('Petiscos do Cardeal',         'Marisco'),
    ('Petiscos do Cardeal',         'Casco histórico'),
    ('El Papatorio Tapas y Brasas', 'Pulpo'),
    ('El Papatorio Tapas y Brasas', 'Raciones'),
    ('El Papatorio Tapas y Brasas', 'Casco histórico'),
    ('Bodegón de Xulio',            'Tradicional'),
    ('Bodegón de Xulio',            'Casco histórico'),
    ('Mesón 42',                    'Tradicional'),
    ('Mesón 42',                    'Pulpo'),
    ('Mesón 42',                    'Casco histórico'),
    ('Bar La Tita',                 'Tapa gratis'),
    ('Bar La Tita',                 'Tortilla'),
    ('Bar La Tita',                 'Casco histórico'),
    ('Raíces Galegas',              'Tortilla'),
    ('Raíces Galegas',              'Ensanche')
) AS v (business, tag)
JOIN businesses b ON b.name = v.business
JOIN tags t ON t.name = v.tag;

-- Abastos 2.0: lunes a sábado, 12:00-15:30 y 20:00-23:00 (domingo cerrado)
INSERT INTO opening_hours (business_id, day_of_week, opens_at, closes_at)
SELECT b.id, d, v.opens_at::time, v.closes_at::time
FROM (VALUES ('12:00', '15:30'), ('20:00', '23:00')) AS v (opens_at, closes_at)
CROSS JOIN generate_series(1, 6) AS d
JOIN businesses b ON b.name = 'Abastos 2.0';

-- Tabernas y bares (orientativo): todos los días, mediodía y noche.
-- 00:00 o 01:00 como cierre = la franja cruza la medianoche.
INSERT INTO opening_hours (business_id, day_of_week, opens_at, closes_at)
SELECT b.id, d, v.opens_at::time, v.closes_at::time
FROM (VALUES
    ('O Gato Negro',                '12:00', '16:00'),
    ('O Gato Negro',                '20:00', '00:00'),
    ('Bar Orella',                  '12:00', '16:00'),
    ('Bar Orella',                  '20:00', '00:00'),
    ('Viñoteca Ventosela',          '12:30', '16:00'),
    ('Viñoteca Ventosela',          '19:30', '01:00'),
    ('Bar Trafalgar',               '12:00', '16:00'),
    ('Bar Trafalgar',               '20:00', '00:00'),
    ('Taberna O Celme do Caracol',  '13:00', '16:00'),
    ('Taberna O Celme do Caracol',  '20:00', '23:30'),
    ('A Taberna do Bispo',          '12:00', '16:30'),
    ('A Taberna do Bispo',          '19:30', '00:00'),
    ('Petiscos do Cardeal',         '12:00', '16:00'),
    ('Petiscos do Cardeal',         '20:00', '23:30'),
    ('El Papatorio Tapas y Brasas', '12:00', '23:00'),
    ('Bodegón de Xulio',            '12:30', '16:00'),
    ('Bodegón de Xulio',            '20:00', '23:30'),
    ('Mesón 42',                    '12:00', '16:00'),
    ('Mesón 42',                    '20:00', '23:30'),
    ('Bar La Tita',                 '12:00', '00:00')
) AS v (business, opens_at, closes_at)
JOIN businesses b ON b.name = v.business
CROSS JOIN generate_series(1, 7) AS d;

-- Locales del Ensanche (orientativo): cerrados el domingo
INSERT INTO opening_hours (business_id, day_of_week, opens_at, closes_at)
SELECT b.id, d, v.opens_at::time, v.closes_at::time
FROM (VALUES
    ('A Taberna do Ensanche', '12:00', '16:30'),
    ('A Taberna do Ensanche', '20:00', '23:30'),
    ('Raíces Galegas',        '08:00', '16:00'),
    ('Raíces Galegas',        '19:00', '23:30')
) AS v (business, opens_at, closes_at)
JOIN businesses b ON b.name = v.business
CROSS JOIN generate_series(1, 6) AS d;
