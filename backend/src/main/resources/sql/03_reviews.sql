-- Reseñas. Adrián no ha reseñado Casa Pepe da Troia ni O Filandón (de Hugo): puede escribir una y ganar 10 puntos.
-- Pulpería A Quintana no tiene ninguna: sale en el mapa como "Nuevo"
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
    ('maria_tapas', 'Bar La Tita',           3, 'La tortilla está bien, el local muy lleno.',    5),
    ('lucia_sdc',    'O Gato Negro',            5, 'El ribeiro en cunca y el pulpo, como siempre.',                    22),
    ('brais_rua',    'O Gato Negro',            4, 'Hay que esperar un poco para coger sitio, pero merece la pena.',   18),
    ('antia_galega', 'O Gato Negro',            5, 'La mejor taberna de la Raíña, sin discusión.',                      9),
    ('lucia_sdc',    'A Taberna do Ensanche',   4, 'Los guisos de cuchara están buenísimos.',                          16),
    ('noa_compos',   'A Taberna do Ensanche',   3, 'Buen producto, aunque el servicio fue algo lento.',                 7),
    ('antia_galega', 'Abastos 2.0',             5, 'Pedimos el menú degustación: todo de diez.',                       11),
    ('iago_tapas',   'Bar Orella',              4, 'La oreja a la plancha, crujiente por fuera.',                       6),
    ('pablo_franco', 'Bar Orella',              4, 'Barato y con tapa con cada consumición.',                           4),
    ('noa_compos',   'Viñoteca Ventosela',      5, 'Vino bueno y el embutido no para de salir.',                       13),
    ('lucia_sdc',    'A Taberna do Bispo',      5, 'Las tostas de pulpo con queso, espectaculares.',                   20),
    ('brais_rua',    'A Taberna do Bispo',      4, 'Mucha variedad de pinchos en la barra.',                           12),
    ('adrianql5',    'A Taberna do Bispo',      4, 'Muy buena relación calidad-precio para estar al lado de la Catedral.', 3),
    ('iago_tapas',   'Petiscos do Cardeal',     4, 'Las zamburiñas a la plancha muy ricas.',                           10),
    ('pablo_franco', 'Bar La Tita',             4, 'La tortilla está muy jugosa.',                                      8),
    ('noa_compos',   'Bar La Tita',             4, 'Para un vino rápido con tortilla, perfecto.',                       2),
    ('lucia_sdc',    'Casa Pepe da Troia',      5, 'La empanada de zamburiñas es de las mejores de Santiago.',         14),
    ('iago_tapas',   'Casa Pepe da Troia',      4, 'Bar de toda la vida, muy buen trato.',                              5),
    ('brais_rua',    'O Filandón',              5, 'Pides un vino y te ponen queso de tetilla. Imprescindible.',       17),
    ('antia_galega', 'O Filandón',              4, 'Muy pequeño y con mucho ambiente.',                                 3),
    ('adrianql5',    'Mesón do Vilar',          4, 'El raxo con patatas, abundante.',                                  19),
    ('pablo_franco', 'Mesón do Vilar',          3, 'Correcto, algo turístico.',                                        11),
    ('noa_compos',   'A Moa',                   5, 'La terraza es una maravilla en verano.',                           15),
    ('antia_galega', 'A Moa',                   4, 'Caro, pero la cocina está a la altura.',                            6),
    ('lucia_sdc',    'Taberna do Curro',        4, 'Los mejillones de los sábados, de vicio.',                          9),
    ('iago_tapas',   'Bodeguilla de San Roque', 5, 'Gran carta de vinos por copas.',                                   12),
    ('adrianql5',    'O Dezaseis',              5, 'El caldo de los domingos sabe a casa.',                            26),
    ('brais_rua',    'O Dezaseis',              4, 'Raciones grandes, ideal para ir en grupo.',                         8),
    ('pablo_franco', 'Café Casino',             4, 'Un sitio precioso para un café después de pasear.',                 7),
    ('noa_compos',   'Cervexaría Ensanche',     4, 'Buena selección de cervezas gallegas.',                             5),
    ('iago_tapas',   'Cervexaría Ensanche',     3, 'Las hamburguesas bien; mucho ruido.',                               2),
    ('antia_galega', 'Raxo e Zorza',            5, 'La zorza con pan de Cea, brutal.',                                  4),
    ('lucia_sdc',    'Raxo e Zorza',            4, 'Terraza al sol y tapa con cada caña.',                              1),
    ('brais_rua',    'Horta do Obradoiro',      4, 'Hasta los que comemos carne salimos encantados.',                  10)
) AS v (username, business, rating, comment, days_ago)
JOIN users u ON u.username = v.username
JOIN businesses b ON b.name = v.business;

-- La media y el número de reseñas de cada negocio
UPDATE businesses b SET
    rating_count = (SELECT COUNT(*) FROM reviews r WHERE r.business_id = b.id),
    average_rating = COALESCE((SELECT ROUND(AVG(r.rating), 2) FROM reviews r WHERE r.business_id = b.id), 0);