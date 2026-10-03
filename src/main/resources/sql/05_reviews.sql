-- =====================================================================
-- 05 · Reseñas, comentarios y likes
-- Reseñas FICTICIAS escritas por los usuarios de prueba.
-- Al final se recalculan average_rating y rating_count de cada local,
-- igual que hará ReviewService en la aplicación.
-- =====================================================================

-- Reseñas de locales (product_id NULL)
INSERT INTO reviews (user_id, business_id, rating, comment, created_at)
SELECT u.id, b.id, v.rating, v.comment, NOW() - make_interval(days => v.days_ago)
FROM (VALUES
    ('adrianql5',       'O Gato Negro',                5, 'De lo más auténtico de la Raíña. Ambiente de toda la vida.',      28),
    ('adrianql5',       'Bar La Tita',                 4, 'La tortilla merece la cola. Mucha gente a ciertas horas.',         20),
    ('adrianql5',       'Bar Trafalgar',               4, 'Los tigres pican de verdad. Sitio pequeño pero con encanto.',      15),
    ('adrianql5',       'Abastos 2.0',                 5, 'Producto de mercado espectacular. Conviene reservar.',             10),
    ('adrianql5',       'Viñoteca Ventosela',          4, 'Buen vino por copas y la tapa de embutido está muy bien cortada.',  6),
    ('maria_tapas',     'O Gato Negro',                4, 'Raciones generosas a buen precio.',                                25),
    ('maria_tapas',     'El Papatorio Tapas y Brasas', 5, 'El pulpo a la brasa es de diez.',                                  18),
    ('maria_tapas',     'A Taberna do Ensanche',       4, 'Muy buen caldo. Un sitio tranquilo fuera del casco histórico.',     9),
    ('xoan_compos',     'Bar Orella',                  5, 'La oreja como tiene que ser. Un clásico.',                         30),
    ('xoan_compos',     'Viñoteca Ventosela',          5, 'Para empezar la noche por la Raíña.',                              22),
    ('xoan_compos',     'Mesón 42',                    3, 'Correcto, aunque a la hora punta tardan bastante.',                14),
    ('xoan_compos',     'Raíces Galegas',              5, 'El bocadillo de tortilla y zorza no tiene rival.',                  7),
    ('laura_peregrina', 'Bar La Tita',                 5, 'Primera parada después de llegar a la Catedral. Repetiría.',       12),
    ('laura_peregrina', 'A Taberna do Bispo',          4, 'Muchísima variedad de tapas y buen trato.',                         5),
    ('hugoocoto',       'Abastos 2.0',                 5, 'Un referente para cualquiera que trabaje en hostelería.',          16)
) AS v (username, business, rating, comment, days_ago)
JOIN users u ON u.username = v.username
JOIN businesses b ON b.name = v.business;

-- Reseñas de productos (business_id debe ser el del producto: lo garantiza la FK compuesta)
INSERT INTO reviews (user_id, business_id, product_id, rating, comment, created_at)
SELECT u.id, p.business_id, p.id, v.rating, v.comment, NOW() - make_interval(days => v.days_ago)
FROM (VALUES
    ('adrianql5',       'Bar La Tita',                 'Pincho de tortilla',            5, 'Poco hecha, como debe ser.',                  20),
    ('adrianql5',       'Bar Trafalgar',               'Tigres rabiosos',               4, 'Picantes y con mucho relleno.',               15),
    ('maria_tapas',     'El Papatorio Tapas y Brasas', 'Pulpo a la brasa',              5, 'Tierno y con el punto justo de brasa.',       18),
    ('xoan_compos',     'Raíces Galegas',              'Bocadillo de tortilla y zorza', 5, 'Imprescindible.',                              7),
    ('laura_peregrina', 'A Taberna do Bispo',          'Vieira gratinada',              4, 'Muy rica, aunque me quedé con ganas de otra.', 5)
) AS v (username, business, product, rating, comment, days_ago)
JOIN users u ON u.username = v.username
JOIN businesses b ON b.name = v.business
JOIN products p ON p.business_id = b.id AND p.name = v.product;

-- Respuesta del propietario (hugoocoto) a una reseña de su local
UPDATE reviews r
SET reply = 'Gracias, María. Os esperamos pronto con el pulpo recién hecho.',
    replied_at = r.created_at + INTERVAL '1 day'
FROM users u, businesses b
WHERE r.user_id = u.id AND u.username = 'maria_tapas'
  AND r.business_id = b.id AND b.name = 'O Gato Negro'
  AND r.product_id IS NULL;

-- Comentarios sobre reseñas de locales
INSERT INTO review_comments (author_id, review_id, content, created_at)
SELECT a.id, r.id, v.content, r.created_at + make_interval(hours => v.hours_after)
FROM (VALUES
    ('xoan_compos',     'adrianql5',   'O Gato Negro', 'Totalmente de acuerdo, el mejor de la zona.',  5),
    ('maria_tapas',     'adrianql5',   'Abastos 2.0',  'Tengo pendiente ir. ¿Qué pediste?',            3),
    ('adrianql5',       'adrianql5',   'Abastos 2.0',  'Pulpo y zamburiñas, ¡no falla!',               6),
    ('laura_peregrina', 'xoan_compos', 'Bar Orella',   'Me lo apunto para la próxima.',               20)
) AS v (author, reviewer, business, content, hours_after)
JOIN users a  ON a.username  = v.author
JOIN users ru ON ru.username = v.reviewer
JOIN businesses b ON b.name = v.business
JOIN reviews r ON r.user_id = ru.id AND r.business_id = b.id AND r.product_id IS NULL;

-- Comentario sobre una reseña de producto
INSERT INTO review_comments (author_id, review_id, content, created_at)
SELECT a.id, r.id, 'Coincido, el mejor pulpo de la Rúa do Franco.', r.created_at + INTERVAL '2 hours'
FROM users a, users ru, products p, reviews r
WHERE a.username = 'adrianql5' AND ru.username = 'maria_tapas'
  AND p.name = 'Pulpo a la brasa'
  AND p.business_id = (SELECT id FROM businesses WHERE name = 'El Papatorio Tapas y Brasas')
  AND r.user_id = ru.id AND r.product_id = p.id;

-- Likes sobre reseñas de locales
INSERT INTO review_likes (user_id, review_id, created_at)
SELECT l.id, r.id, r.created_at + INTERVAL '1 hour'
FROM (VALUES
    ('maria_tapas',     'adrianql5',   'O Gato Negro'),
    ('xoan_compos',     'adrianql5',   'O Gato Negro'),
    ('laura_peregrina', 'adrianql5',   'Abastos 2.0'),
    ('adrianql5',       'maria_tapas', 'El Papatorio Tapas y Brasas'),
    ('adrianql5',       'xoan_compos', 'Raíces Galegas'),
    ('maria_tapas',     'xoan_compos', 'Bar Orella')
) AS v (liker, reviewer, business)
JOIN users l  ON l.username  = v.liker
JOIN users ru ON ru.username = v.reviewer
JOIN businesses b ON b.name = v.business
JOIN reviews r ON r.user_id = ru.id AND r.business_id = b.id AND r.product_id IS NULL;

-- Recalcular la media y el número de reseñas de cada local
-- (solo cuentan las reseñas del local, no las de sus productos)
UPDATE businesses b SET
    average_rating = COALESCE((SELECT ROUND(AVG(r.rating), 2) FROM reviews r
                               WHERE r.business_id = b.id AND r.product_id IS NULL), 0),
    rating_count   = (SELECT COUNT(*) FROM reviews r
                      WHERE r.business_id = b.id AND r.product_id IS NULL);
