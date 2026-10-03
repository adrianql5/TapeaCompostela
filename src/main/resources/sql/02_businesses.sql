-- =====================================================================
-- 02 · Locales
-- Nombres y direcciones de locales reales de Santiago, tomados de guías
-- de tapeo publicadas. ATENCIÓN:
--  * Coordenadas: las de Abastos 2.0 y A Taberna do Ensanche son las
--    publicadas; el resto son APROXIMADAS (±50-100 m) según la calle.
--    Revisadlas en el mapa y ajustadlas si hace falta.
--  * Qué locales son adheridos y quién es su propietario es FICTICIO:
--    son datos de prueba.
--  * Teléfono y web solo donde aparecían publicados.
-- La media y el número de reseñas se calculan en 05_reviews.sql.
-- =====================================================================

INSERT INTO businesses (name, description, address, latitude, longitude, is_partner, owner_id, price_range, phone, website, created_at) VALUES
-- Locales adheridos de hugoocoto
('O Gato Negro',
 'Taberna clásica de la Rúa da Raíña, sencilla y con mucha clientela local. Raciones de cocina gallega de siempre.',
 'Rúa da Raíña, s/n, Santiago de Compostela', 42.8792, -8.5445,
 TRUE, (SELECT id FROM users WHERE username = 'hugoocoto'), 'CHEAP', NULL, NULL, NOW() - INTERVAL '85 days'),
('A Taberna do Ensanche',
 'Tapas de buen producto para acompañar vino o cerveza, con caldos y guisos. En pleno Ensanche.',
 'Rúa de Santiago de Chile, 12, 15701 Santiago de Compostela', 42.8725087, -8.5508864,
 TRUE, (SELECT id FROM users WHERE username = 'hugoocoto'), 'MODERATE', '981590788', NULL, NOW() - INTERVAL '80 days'),
-- Locales normales (sin propietario en la app)
('Abastos 2.0',
 'Cocina de mercado dentro del Mercado de Abastos: el menú cambia según el producto del día.',
 'Rúa das Ameas, 13-18 (Mercado de Abastos), 15704 Santiago de Compostela', 42.879944, -8.541528,
 FALSE, NULL, 'EXPENSIVE', '981576145', 'http://www.abastosdouspuntocero.es/', NOW() - INTERVAL '75 days'),
('Bar Orella',
 'Taberna de aire muy local cuya tapa protagonista es la oreja de cerdo.',
 'Rúa da Raíña, 21, Santiago de Compostela', 42.8789, -8.5444,
 FALSE, NULL, 'CHEAP', NULL, NULL, NOW() - INTERVAL '75 days'),
('Viñoteca Ventosela',
 'Vinoteca clásica de la Raíña. Con el vino ponen un plato de embutido y queso.',
 'Rúa da Raíña, 28, Santiago de Compostela', 42.8786, -8.5444,
 FALSE, NULL, 'CHEAP', NULL, NULL, NOW() - INTERVAL '74 days'),
('Bar Trafalgar',
 'Tasca tradicional famosa por los tigres rabiosos: mejillones con salsa picante.',
 'Rúa da Raíña, Santiago de Compostela', 42.8788, -8.5445,
 FALSE, NULL, 'CHEAP', NULL, NULL, NOW() - INTERVAL '74 days'),
('Taberna O Celme do Caracol',
 'Taberna de cocina gallega y tapas en la Rúa da Raíña.',
 'Rúa da Raíña, 18, Santiago de Compostela', 42.8790, -8.5445,
 FALSE, NULL, 'MODERATE', NULL, NULL, NOW() - INTERVAL '70 days'),
('A Taberna do Bispo',
 'Local familiar con gran variedad de tapas y vinos, a unos 100 metros de la Catedral.',
 'Rúa do Franco, 37, 15702 Santiago de Compostela', 42.8786, -8.5450,
 FALSE, NULL, 'MODERATE', NULL, NULL, NOW() - INTERVAL '70 days'),
('Petiscos do Cardeal',
 'Amplia barra con selección de pinchos y carta en la que destaca el marisco.',
 'Rúa do Franco, 10, 15702 Santiago de Compostela', 42.8796, -8.5451,
 FALSE, NULL, 'MODERATE', NULL, NULL, NOW() - INTERVAL '68 days'),
('El Papatorio Tapas y Brasas',
 'Tapas y cocina casera gallega, con el pulpo a la brasa como especialidad.',
 'Rúa do Franco, 20, 15702 Santiago de Compostela', 42.8792, -8.5450,
 FALSE, NULL, 'MODERATE', NULL, NULL, NOW() - INTERVAL '65 days'),
('Bodegón de Xulio',
 'Bodegón tradicional en plena Rúa do Franco.',
 'Rúa do Franco, 27, 15702 Santiago de Compostela', 42.8790, -8.5451,
 FALSE, NULL, 'MODERATE', NULL, NULL, NOW() - INTERVAL '65 days'),
('Mesón 42',
 'Mesón rústico gallego con buenos precios y variedad.',
 'Rúa do Franco, 42, 15702 Santiago de Compostela', 42.8784, -8.5449,
 FALSE, NULL, 'CHEAP', NULL, NULL, NOW() - INTERVAL '60 days'),
('Bar La Tita',
 'Famoso por el pincho de tortilla que acompaña a cada consumición.',
 'Rúa Nova, 46, Santiago de Compostela', 42.8781, -8.5437,
 FALSE, NULL, 'CHEAP', NULL, NULL, NOW() - INTERVAL '60 days'),
('Raíces Galegas',
 'Bar del Ensanche célebre por su bocadillo de tortilla y zorza.',
 'Rúa Nova de Abaixo, 36, Santiago de Compostela', 42.8738, -8.5483,
 FALSE, NULL, 'CHEAP', NULL, NULL, NOW() - INTERVAL '55 days');
