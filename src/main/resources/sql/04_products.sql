-- =====================================================================
-- 04 · Productos
-- Platos típicos de cada local según las guías consultadas.
-- Los PRECIOS son ORIENTATIVOS: datos de prueba, no la carta real.
-- Categorías: TAPA, PORTION (ración), PINCHO, DRINK, DESSERT.
-- =====================================================================

INSERT INTO products (business_id, name, description, price, category)
SELECT b.id, v.name, v.description, v.price, v.category
FROM (VALUES
    ('O Gato Negro',                'Hígado encebollado',       'Clásico de la casa',                              7.50, 'PORTION'),
    ('O Gato Negro',                'Pulpo á feira',            'Con aceite, sal gorda y pimentón',               14.00, 'PORTION'),
    ('O Gato Negro',                'Ribeiro (taza)',           'Vino blanco del Ribeiro servido en cunca',        1.80, 'DRINK'),
    ('A Taberna do Ensanche',       'Caldo gallego',            'Caldo de grelos con patata',                      5.00, 'PORTION'),
    ('A Taberna do Ensanche',       'Pulpo á feira',            'Ración para compartir',                          15.00, 'PORTION'),
    ('A Taberna do Ensanche',       'Empanada de zamburiñas',   'Porción de empanada casera',                      4.50, 'PINCHO'),
    ('Abastos 2.0',                 'Pulpo a la brasa',         'Con producto del mercado',                       18.00, 'PORTION'),
    ('Abastos 2.0',                 'Zamburiñas a la plancha',  'Según disponibilidad del día',                   12.00, 'PORTION'),
    ('Bar Orella',                  'Oreja de cerdo',           'Cocida, con aceite y pimentón',                   6.00, 'PORTION'),
    ('Bar Orella',                  'Albariño (copa)',          NULL,                                              2.50, 'DRINK'),
    ('Viñoteca Ventosela',          'Tabla de embutido y queso','Chorizo, salchichón y queso',                     8.00, 'PORTION'),
    ('Viñoteca Ventosela',          'Roxóns',                   'Chicharrones de cerdo',                           6.50, 'PORTION'),
    ('Viñoteca Ventosela',          'Vino de Betanzos (copa)',  NULL,                                              2.20, 'DRINK'),
    ('Bar Trafalgar',               'Tigres rabiosos',          'Mejillones rellenos con salsa picante',           6.00, 'PORTION'),
    ('Taberna O Celme do Caracol',  'Croquetas caseras',        NULL,                                              8.00, 'PORTION'),
    ('Taberna O Celme do Caracol',  'Pimientos de Padrón',      'Unos pican y otros no',                           6.00, 'PORTION'),
    ('A Taberna do Bispo',          'Montadito de zorza',       NULL,                                              2.50, 'PINCHO'),
    ('A Taberna do Bispo',          'Vieira gratinada',         NULL,                                              4.00, 'TAPA'),
    ('A Taberna do Bispo',          'Mejillones al vapor',      NULL,                                              7.00, 'PORTION'),
    ('Petiscos do Cardeal',         'Pincho de pulpo',          NULL,                                              3.50, 'PINCHO'),
    ('Petiscos do Cardeal',         'Navajas a la plancha',     NULL,                                             13.00, 'PORTION'),
    ('El Papatorio Tapas y Brasas', 'Pulpo a la brasa',         'La especialidad de la casa',                     16.00, 'PORTION'),
    ('El Papatorio Tapas y Brasas', 'Tarta de Santiago',        NULL,                                              4.50, 'DESSERT'),
    ('Bodegón de Xulio',            'Empanada de bacalao',      'Porción',                                         4.00, 'PINCHO'),
    ('Mesón 42',                    'Pulpo á feira',            NULL,                                             13.00, 'PORTION'),
    ('Mesón 42',                    'Lacón con grelos',         NULL,                                             11.00, 'PORTION'),
    ('Bar La Tita',                 'Pincho de tortilla',       'Jugosa; sale de tapa con la consumición',         2.50, 'PINCHO'),
    ('Bar La Tita',                 'Estrella Galicia (caña)',  NULL,                                              2.00, 'DRINK'),
    ('Raíces Galegas',              'Bocadillo de tortilla y zorza', 'El más famoso de la casa',                   5.50, 'PINCHO'),
    ('Raíces Galegas',              'Café',                     NULL,                                              1.40, 'DRINK')
) AS v (business, name, description, price, category)
JOIN businesses b ON b.name = v.business;

-- ---------------------------------------------------------------------
-- Allergens (the 14 of EU Regulation 1169/2011) and the ones in each
-- product. The product-allergen pairs are an ESTIMATE from the usual
-- recipe, not the venues' real allergen charts.
-- ---------------------------------------------------------------------

INSERT INTO allergens (code, name) VALUES
('GLUTEN',      'Cereales con gluten'),
('CRUSTACEANS', 'Crustáceos'),
('EGGS',        'Huevos'),
('FISH',        'Pescado'),
('PEANUTS',     'Cacahuetes'),
('SOYBEANS',    'Soja'),
('MILK',        'Leche'),
('NUTS',        'Frutos de cáscara'),
('CELERY',      'Apio'),
('MUSTARD',     'Mostaza'),
('SESAME',      'Granos de sésamo'),
('SULPHITES',   'Dióxido de azufre y sulfitos'),
('LUPIN',       'Altramuces'),
('MOLLUSCS',    'Moluscos');

INSERT INTO product_allergens (product_id, allergen_id)
SELECT p.id, a.id
FROM (VALUES
    ('O Gato Negro',                'Pulpo á feira',                 'MOLLUSCS'),
    ('O Gato Negro',                'Ribeiro (taza)',                'SULPHITES'),
    ('A Taberna do Ensanche',       'Pulpo á feira',                 'MOLLUSCS'),
    ('A Taberna do Ensanche',       'Empanada de zamburiñas',        'GLUTEN'),
    ('A Taberna do Ensanche',       'Empanada de zamburiñas',        'EGGS'),
    ('A Taberna do Ensanche',       'Empanada de zamburiñas',        'MOLLUSCS'),
    ('Abastos 2.0',                 'Pulpo a la brasa',              'MOLLUSCS'),
    ('Abastos 2.0',                 'Zamburiñas a la plancha',       'MOLLUSCS'),
    ('Bar Orella',                  'Albariño (copa)',               'SULPHITES'),
    ('Viñoteca Ventosela',          'Tabla de embutido y queso',     'MILK'),
    ('Viñoteca Ventosela',          'Vino de Betanzos (copa)',       'SULPHITES'),
    ('Bar Trafalgar',               'Tigres rabiosos',               'MOLLUSCS'),
    ('Bar Trafalgar',               'Tigres rabiosos',               'GLUTEN'),
    ('Bar Trafalgar',               'Tigres rabiosos',               'MILK'),
    ('Bar Trafalgar',               'Tigres rabiosos',               'EGGS'),
    ('Taberna O Celme do Caracol',  'Croquetas caseras',             'GLUTEN'),
    ('Taberna O Celme do Caracol',  'Croquetas caseras',             'MILK'),
    ('Taberna O Celme do Caracol',  'Croquetas caseras',             'EGGS'),
    ('A Taberna do Bispo',          'Montadito de zorza',            'GLUTEN'),
    ('A Taberna do Bispo',          'Vieira gratinada',              'MOLLUSCS'),
    ('A Taberna do Bispo',          'Vieira gratinada',              'GLUTEN'),
    ('A Taberna do Bispo',          'Vieira gratinada',              'MILK'),
    ('A Taberna do Bispo',          'Mejillones al vapor',           'MOLLUSCS'),
    ('Petiscos do Cardeal',         'Pincho de pulpo',               'MOLLUSCS'),
    ('Petiscos do Cardeal',         'Pincho de pulpo',               'GLUTEN'),
    ('Petiscos do Cardeal',         'Navajas a la plancha',          'MOLLUSCS'),
    ('El Papatorio Tapas y Brasas', 'Pulpo a la brasa',              'MOLLUSCS'),
    ('El Papatorio Tapas y Brasas', 'Tarta de Santiago',             'NUTS'),
    ('El Papatorio Tapas y Brasas', 'Tarta de Santiago',             'EGGS'),
    ('Bodegón de Xulio',            'Empanada de bacalao',           'GLUTEN'),
    ('Bodegón de Xulio',            'Empanada de bacalao',           'EGGS'),
    ('Bodegón de Xulio',            'Empanada de bacalao',           'FISH'),
    ('Mesón 42',                    'Pulpo á feira',                 'MOLLUSCS'),
    ('Bar La Tita',                 'Pincho de tortilla',            'EGGS'),
    ('Bar La Tita',                 'Estrella Galicia (caña)',       'GLUTEN'),
    ('Raíces Galegas',              'Bocadillo de tortilla y zorza', 'GLUTEN'),
    ('Raíces Galegas',              'Bocadillo de tortilla y zorza', 'EGGS')
) AS v (business, product, allergen)
JOIN businesses b ON b.name = v.business
JOIN products p ON p.business_id = b.id AND p.name = v.product
JOIN allergens a ON a.code = v.allergen;
