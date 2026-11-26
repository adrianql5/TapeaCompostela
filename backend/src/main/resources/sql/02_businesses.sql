-- Bares de Santiago
INSERT INTO businesses (name, description, address, latitude, longitude, price_range, phone, owner_id, created_at) VALUES
('O Gato Negro', 'Taberna clásica de la Rúa da Raíña, sencilla y con mucha clientela local.',
 'Rúa da Raíña, s/n', 42.8792, -8.5445, 'CHEAP', NULL, (SELECT id FROM users WHERE username = 'hugoocoto'), NOW() - INTERVAL '85 days'),
('A Taberna do Ensanche', 'Tapas de buen producto para acompañar vino o cerveza, con caldos y guisos.',
 'Rúa de Santiago de Chile, 12', 42.8725, -8.5509, 'MODERATE', '981590788', (SELECT id FROM users WHERE username = 'hugoocoto'), NOW() - INTERVAL '80 days'),
('Abastos 2.0', 'Cocina de mercado dentro del Mercado de Abastos: el menú cambia según el producto del día.',
 'Rúa das Ameas, 13-18', 42.8799, -8.5415, 'EXPENSIVE', '981576145', NULL, NOW() - INTERVAL '75 days'),
('Bar Orella', 'Taberna de aire muy local cuya tapa protagonista es la oreja de cerdo.',
 'Rúa da Raíña, 21', 42.8789, -8.5444, 'CHEAP', NULL, NULL, NOW() - INTERVAL '75 days'),
('Viñoteca Ventosela', 'Vinoteca clásica de la Raíña. Con el vino ponen un plato de embutido y queso.',
 'Rúa da Raíña, 28', 42.8786, -8.5444, 'CHEAP', NULL, NULL, NOW() - INTERVAL '74 days'),
('A Taberna do Bispo', 'Local familiar con gran variedad de tapas y vinos, a unos 100 metros de la Catedral.',
 'Rúa do Franco, 37', 42.8786, -8.5450, 'MODERATE', NULL, NULL, NOW() - INTERVAL '70 days'),
('Petiscos do Cardeal', 'Amplia barra con selección de pinchos y carta en la que destaca el marisco.',
 'Rúa do Franco, 10', 42.8796, -8.5451, 'MODERATE', NULL, NULL, NOW() - INTERVAL '68 days'),
('Bar La Tita', 'Famoso por el pincho de tortilla que acompaña a cada consumición.',
 'Rúa Nova, 46', 42.8781, -8.5437, 'CHEAP', NULL, NULL, NOW() - INTERVAL '60 days');

INSERT INTO tags (name) VALUES
('Tapa gratis'), ('Pulpo'), ('Marisco'), ('Vinos'), ('Tortilla'), ('Tradicional'), ('Casco histórico'), ('Ensanche');

INSERT INTO business_tags (business_id, tag_id)
SELECT b.id, t.id
FROM (VALUES
    ('O Gato Negro',          'Tradicional'),
    ('O Gato Negro',          'Pulpo'),
    ('O Gato Negro',          'Casco histórico'),
    ('A Taberna do Ensanche', 'Vinos'),
    ('A Taberna do Ensanche', 'Ensanche'),
    ('Abastos 2.0',           'Marisco'),
    ('Abastos 2.0',           'Casco histórico'),
    ('Bar Orella',            'Tapa gratis'),
    ('Bar Orella',            'Tradicional'),
    ('Bar Orella',            'Casco histórico'),
    ('Viñoteca Ventosela',    'Tapa gratis'),
    ('Viñoteca Ventosela',    'Vinos'),
    ('Viñoteca Ventosela',    'Casco histórico'),
    ('A Taberna do Bispo',    'Vinos'),
    ('A Taberna do Bispo',    'Casco histórico'),
    ('Petiscos do Cardeal',   'Marisco'),
    ('Petiscos do Cardeal',   'Casco histórico'),
    ('Bar La Tita',           'Tortilla'),
    ('Bar La Tita',           'Tapa gratis')
) AS v (business, tag)
JOIN businesses b ON b.name = v.business
JOIN tags t ON t.name = v.tag;