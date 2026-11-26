INSERT INTO reviews (user_id, business_id, rating, comment, created_at)
SELECT u.id, b.id, v.rating, v.comment, NOW() - make_interval(days => v.days_ago)
FROM (VALUES
    ('adrianql5',   'O Gato Negro',          5, 'De lo más auténtico de la Raíña.',             30),
    ('maria_tapas', 'O Gato Negro',          4, 'Raciones generosas a buen precio.',            25),
    ('xoan_compos', 'A Taberna do Ensanche', 4, 'Buenos vinos y mejor caldo.',                  20),
    ('adrianql5',   'Abastos 2.0',           5, 'Producto del mercado, impecable.',             15),
    ('maria_tapas', 'Abastos 2.0',           4, 'Caro pero vale la pena.',                      12),
    ('xoan_compos', 'Bar Orella',            5, 'La oreja, imprescindible.',                    10),
    ('adrianql5',   'Viñoteca Ventosela',    4, 'El plato de embutido con el vino, un acierto.', 8),
    ('maria_tapas', 'Bar La Tita',           3, 'La tortilla está bien, el local muy lleno.',    5)
) AS v (username, business, rating, comment, days_ago)
JOIN users u ON u.username = v.username
JOIN businesses b ON b.name = v.business;

-- La media y el número de reseñas de cada negocio
UPDATE businesses b SET
    rating_count = (SELECT COUNT(*) FROM reviews r WHERE r.business_id = b.id),
    average_rating = COALESCE((SELECT ROUND(AVG(r.rating), 2) FROM reviews r WHERE r.business_id = b.id), 0);