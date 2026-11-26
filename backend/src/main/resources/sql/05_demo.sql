-- Datos de más para la demo con dos móviles. Solo se cargan con el perfil "demo" (application-demo.properties),
-- después de los scripts 01-04: así las respuestas de ejemplo de las guías, que usan solo los datos de 01-04, no cambian.
-- Todos los usuarios tienen la contraseña "password"

-- Clientes y dueños nuevos
INSERT INTO users (username, name, email, password, points, created_at) VALUES
('lucia_sdc',     'Lucía',   'lucia_sdc@example.com',     '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 70,  NOW() - INTERVAL '50 days'),
('brais_rua',     'Brais',   'brais_rua@example.com',     '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 30,  NOW() - INTERVAL '38 days'),
('antia_galega',  'Antía',   'antia_galega@example.com',  '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 120, NOW() - INTERVAL '35 days'),
('pablo_franco',  'Pablo',   'pablo_franco@example.com',  '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 20,  NOW() - INTERVAL '30 days'),
('noa_compos',    'Noa',     'noa_compos@example.com',    '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 60,  NOW() - INTERVAL '22 days'),
('iago_tapas',    'Iago',    'iago_tapas@example.com',    '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 40,  NOW() - INTERVAL '15 days'),
('carmen_raina',  'Carmen',  'carmen_raina@example.com',  '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 0,   NOW() - INTERVAL '100 days'),
('manolo_abastos','Manolo',  'manolo_abastos@example.com','$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 0,   NOW() - INTERVAL '95 days');

INSERT INTO user_roles (user_id, role_name)
SELECT u.id, v.role
FROM (VALUES
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

-- Adrián empieza con puntos para canjear varias veces durante la demo
UPDATE users SET points = 200 WHERE username = 'adrianql5';

-- Los locales de 02 sin teléfono ni dueño: en la demo todos tienen las dos cosas. Hugo es el dueño de O Gato Negro
-- y de A Taberna do Ensanche (de 02) y de dos más de aquí abajo
UPDATE businesses SET phone = '981 58 91 23' WHERE name = 'O Gato Negro';
UPDATE businesses SET phone = '981 57 26 30', owner_id = (SELECT id FROM users WHERE username = 'carmen_raina') WHERE name = 'Bar Orella';
UPDATE businesses SET phone = '981 58 80 14', owner_id = (SELECT id FROM users WHERE username = 'carmen_raina') WHERE name = 'Viñoteca Ventosela';
UPDATE businesses SET phone = '981 58 18 45', owner_id = (SELECT id FROM users WHERE username = 'carmen_raina') WHERE name = 'A Taberna do Bispo';
UPDATE businesses SET phone = '981 56 33 21', owner_id = (SELECT id FROM users WHERE username = 'carmen_raina') WHERE name = 'Petiscos do Cardeal';
UPDATE businesses SET phone = '981 58 49 52', owner_id = (SELECT id FROM users WHERE username = 'carmen_raina') WHERE name = 'Bar La Tita';
UPDATE businesses SET owner_id = (SELECT id FROM users WHERE username = 'manolo_abastos') WHERE name = 'Abastos 2.0';

-- Más locales, repartidos por el casco histórico, el Ensanche y San Pedro
INSERT INTO businesses (name, description, address, latitude, longitude, price_range, phone, owner_id, created_at) VALUES
('Casa Pepe da Troia', 'Bar pequeño de la Rúa da Troia con empanada casera y vino de Ribeiro en cunca.',
 'Rúa da Troia, 8', 42.8818, -8.5428, 'CHEAP', '981 58 22 04', (SELECT id FROM users WHERE username = 'hugoocoto'), NOW() - INTERVAL '58 days'),
('O Filandón', 'Barra estrecha y siempre llena: con cada vino sale una tapa de queso o de chorizo.',
 'Rúa da Acibechería, 6', 42.8814, -8.5440, 'CHEAP', '981 57 25 46', (SELECT id FROM users WHERE username = 'hugoocoto'), NOW() - INTERVAL '55 days'),
('Mesón do Vilar', 'Comedor de piedra en la Rúa do Vilar: pulpo, pimientos de Padrón y raxo.',
 'Rúa do Vilar, 47', 42.8774, -8.5440, 'MODERATE', '981 56 06 17', (SELECT id FROM users WHERE username = 'carmen_raina'), NOW() - INTERVAL '52 days'),
('A Moa', 'Cocina gallega actualizada con terraza en un jardín escondido detrás de la Quintana.',
 'Rúa de San Paio de Antealtares, 14', 42.8806, -8.5420, 'EXPENSIVE', '981 57 34 60', (SELECT id FROM users WHERE username = 'manolo_abastos'), NOW() - INTERVAL '50 days'),
('Taberna do Curro', 'Tapas de mercado al lado de la Praza de Abastos. Los sábados, mejillones al vapor.',
 'Rúa do Curro da Parra, 7', 42.8804, -8.5412, 'MODERATE', '981 55 60 59', (SELECT id FROM users WHERE username = 'manolo_abastos'), NOW() - INTERVAL '47 days'),
('Bodeguilla de San Roque', 'Vinos por copas y raciones para compartir en una bodega de techos bajos.',
 'Rúa de San Roque, 13', 42.8838, -8.5440, 'MODERATE', '981 56 43 79', (SELECT id FROM users WHERE username = 'carmen_raina'), NOW() - INTERVAL '44 days'),
('O Dezaseis', 'Bajo de piedra en San Pedro con caldo, lacón y cocido los fines de semana.',
 'Rúa de San Pedro, 16', 42.8830, -8.5395, 'MODERATE', '981 56 48 80', (SELECT id FROM users WHERE username = 'manolo_abastos'), NOW() - INTERVAL '40 days'),
('Café Casino', 'Café de 1873 con lámparas de araña y sillones: chocolate con churros y tostas.',
 'Rúa do Vilar, 35', 42.8779, -8.5441, 'MODERATE', '981 57 75 03', (SELECT id FROM users WHERE username = 'carmen_raina'), NOW() - INTERVAL '36 days'),
('Cervexaría Ensanche', 'Cervezas gallegas de grifo y hamburguesas de ternera en pleno Ensanche.',
 'Rúa da República Arxentina, 21', 42.8738, -8.5472, 'CHEAP', '981 59 31 12', (SELECT id FROM users WHERE username = 'hugoocoto'), NOW() - INTERVAL '30 days'),
('Raxo e Zorza', 'Tapas de raxo, zorza y croquetas caseras; terraza al sol en la Praza Roxa.',
 'Praza Roxa, 3', 42.8733, -8.5506, 'CHEAP', '981 59 07 41', (SELECT id FROM users WHERE username = 'hugoocoto'), NOW() - INTERVAL '25 days'),
('Horta do Obradoiro', 'Cocina vegetariana de temporada con verduras de las huertas de Conxo.',
 'Rúa das Hortas, 16', 42.8812, -8.5474, 'EXPENSIVE', '981 56 53 16', (SELECT id FROM users WHERE username = 'manolo_abastos'), NOW() - INTERVAL '20 days'),
('Pulpería A Quintana', 'Pulpo á feira cortado con tijera y servido en plato de madera.',
 'Praza da Quintana, 2', 42.8805, -8.5430, 'MODERATE', '981 58 11 90', (SELECT id FROM users WHERE username = 'carmen_raina'), NOW() - INTERVAL '6 days');

INSERT INTO tags (name) VALUES ('Empanada'), ('Terraza'), ('Cervezas'), ('Vegetariano'), ('Raciones');

INSERT INTO business_tags (business_id, tag_id)
SELECT b.id, t.id
FROM (VALUES
    ('Casa Pepe da Troia',      'Empanada'),
    ('Casa Pepe da Troia',      'Vinos'),
    ('Casa Pepe da Troia',      'Casco histórico'),
    ('O Filandón',              'Tapa gratis'),
    ('O Filandón',              'Vinos'),
    ('O Filandón',              'Casco histórico'),
    ('Mesón do Vilar',          'Pulpo'),
    ('Mesón do Vilar',          'Tradicional'),
    ('Mesón do Vilar',          'Casco histórico'),
    ('A Moa',                   'Terraza'),
    ('A Moa',                   'Casco histórico'),
    ('Taberna do Curro',        'Marisco'),
    ('Taberna do Curro',        'Raciones'),
    ('Taberna do Curro',        'Casco histórico'),
    ('Bodeguilla de San Roque', 'Vinos'),
    ('Bodeguilla de San Roque', 'Raciones'),
    ('O Dezaseis',              'Tradicional'),
    ('O Dezaseis',              'Raciones'),
    ('Café Casino',             'Casco histórico'),
    ('Cervexaría Ensanche',     'Cervezas'),
    ('Cervexaría Ensanche',     'Ensanche'),
    ('Raxo e Zorza',            'Terraza'),
    ('Raxo e Zorza',            'Ensanche'),
    ('Raxo e Zorza',            'Tapa gratis'),
    ('Horta do Obradoiro',      'Vegetariano'),
    ('Horta do Obradoiro',      'Terraza'),
    ('Pulpería A Quintana',     'Pulpo'),
    ('Pulpería A Quintana',     'Casco histórico'),
    ('O Gato Negro',            'Vinos'),
    ('A Taberna do Ensanche',   'Raciones')
) AS v (business, tag)
JOIN businesses b ON b.name = v.business
JOIN tags t ON t.name = v.tag;

-- Reseñas. Adrián no ha reseñado Casa Pepe da Troia ni O Filandón (de Hugo): así en la demo puede escribir una y
-- ganar 10 puntos. Pulpería A Quintana no tiene ninguna: sale en el mapa como "Nuevo"
INSERT INTO reviews (user_id, business_id, rating, comment, created_at)
SELECT u.id, b.id, v.rating, v.comment, NOW() - make_interval(days => v.days_ago)
FROM (VALUES
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

-- La media y el número de reseñas de cada local, otra vez con las reseñas nuevas
UPDATE businesses b SET
    rating_count = (SELECT COUNT(*) FROM reviews r WHERE r.business_id = b.id),
    average_rating = COALESCE((SELECT ROUND(AVG(r.rating), 2) FROM reviews r WHERE r.business_id = b.id), 0);

-- Recompensas de los locales nuevos y alguna más para los de 02
INSERT INTO rewards (business_id, title, description, points_cost, active)
SELECT b.id, v.title, v.description, v.cost, TRUE
FROM (VALUES
    ('O Gato Negro',            'Tapa de empanada',          'Un trozo de empanada del día',                   20),
    ('A Taberna do Ensanche',   'Tabla de quesos',           'Arzúa-Ulloa, tetilla y San Simón',               60),
    ('Casa Pepe da Troia',      'Cunca de ribeiro',          'Vino de la casa en cunca',                       20),
    ('Casa Pepe da Troia',      'Ración de empanada',        'Empanada de zamburiñas o de bonito',             50),
    ('O Filandón',              'Vino con tapa doble',       'Dos tapas de queso y chorizo con tu vino',       30),
    ('Cervexaría Ensanche',     'Caña de Estrella Galicia',  'Una caña de grifo',                              20),
    ('Cervexaría Ensanche',     'Hamburguesa de ternera',    'Con patatas fritas',                             90),
    ('Raxo e Zorza',            'Tapa de raxo',              'Raxo con pimientos',                             30),
    ('Abastos 2.0',             'Copa de albariño',          'Para acompañar el menú',                         40),
    ('Bar Orella',              'Ración de oreja',           'Oreja a la plancha con pimentón',                40),
    ('Viñoteca Ventosela',      'Copa de mencía',            'Una copa de vino tinto de la Ribeira Sacra',     20),
    ('A Taberna do Bispo',      'Tosta de pulpo',            'Tosta de pulpo con queso fundido',               40),
    ('Petiscos do Cardeal',     'Zamburiñas',                'Media ración a la plancha',                      80),
    ('Bar La Tita',             'Pincho de tortilla extra',  'Otro pincho de tortilla con tu consumición',     10),
    ('Mesón do Vilar',          'Pimientos de Padrón',       'Unos pican y otros no',                          30),
    ('A Moa',                   'Postre de la casa',         'Tarta de Santiago con helado',                   60),
    ('Taberna do Curro',        'Mejillones al vapor',       'Solo los sábados',                               50),
    ('Bodeguilla de San Roque', 'Copa de vino a elegir',     'Cualquier vino de la carta por copas',           40),
    ('O Dezaseis',              'Cuenco de caldo',           'Caldo gallego de la casa',                       20),
    ('Café Casino',             'Chocolate con churros',     'Para una persona',                               50),
    ('Horta do Obradoiro',      'Crema de temporada',        'La crema de verduras del día',                   40),
    ('Pulpería A Quintana',     'Media de pulpo',            'Media ración de pulpo á feira',                  70)
) AS v (business, title, description, cost)
JOIN businesses b ON b.name = v.business;

-- Canjes de Adrián: uno pendiente en O Gato Negro (de Hugo) para escanearlo nada más empezar la demo, uno ya usado y
-- uno caducado, para que su lista de canjes no esté vacía
INSERT INTO redemptions (user_id, reward_id, code, status, redeemed_at, used_at)
SELECT u.id, r.id, v.code, v.status, NOW() - make_interval(days => v.days_ago),
       CASE WHEN v.status = 'USED' THEN NOW() - make_interval(days => v.days_ago - 1) END
FROM (VALUES
    ('Taza de ribeiro gratis', 'TPC-DEMO0001', 'PENDING', 1),
    ('Cuenco de caldo',        'TPC-DEMO0002', 'USED',    12),
    ('Copa de mencía',         'TPC-DEMO0003', 'EXPIRED', 40)
) AS v (reward, code, status, days_ago)
JOIN users u ON u.username = 'adrianql5'
JOIN rewards r ON r.title = v.reward;
