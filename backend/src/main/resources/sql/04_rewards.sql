INSERT INTO rewards (business_id, title, description, points_cost, active)
SELECT b.id, v.title, v.description, v.cost, v.active
FROM (VALUES
    ('O Gato Negro',          'Taza de ribeiro gratis',  'Una taza del vino de la casa',          30, TRUE),
    ('O Gato Negro',          'Ración de pulpo',         'Una ración de pulpo á feira',          120, TRUE),
    ('A Taberna do Ensanche', 'Caldo gallego',           'Un cuenco de caldo',                    20, TRUE),
    ('A Taberna do Ensanche', 'Postre de la casa',       'Ya no se ofrece',                       25, FALSE),
    ('O Gato Negro',            'Tapa de empanada',          'Un trozo de empanada del día',                   20, TRUE),
    ('A Taberna do Ensanche',   'Tabla de quesos',           'Arzúa-Ulloa, tetilla y San Simón',               60, TRUE),
    ('Casa Pepe da Troia',      'Cunca de ribeiro',          'Vino de la casa en cunca',                       20, TRUE),
    ('Casa Pepe da Troia',      'Ración de empanada',        'Empanada de zamburiñas o de bonito',             50, TRUE),
    ('O Filandón',              'Vino con tapa doble',       'Dos tapas de queso y chorizo con tu vino',       30, TRUE),
    ('Cervexaría Ensanche',     'Caña de Estrella Galicia',  'Una caña de grifo',                              20, TRUE),
    ('Cervexaría Ensanche',     'Hamburguesa de ternera',    'Con patatas fritas',                             90, TRUE),
    ('Raxo e Zorza',            'Tapa de raxo',              'Raxo con pimientos',                             30, TRUE),
    ('Abastos 2.0',             'Copa de albariño',          'Para acompañar el menú',                         40, TRUE),
    ('Bar Orella',              'Ración de oreja',           'Oreja a la plancha con pimentón',                40, TRUE),
    ('Viñoteca Ventosela',      'Copa de mencía',            'Una copa de vino tinto de la Ribeira Sacra',     20, TRUE),
    ('A Taberna do Bispo',      'Tosta de pulpo',            'Tosta de pulpo con queso fundido',               40, TRUE),
    ('Petiscos do Cardeal',     'Zamburiñas',                'Media ración a la plancha',                      80, TRUE),
    ('Bar La Tita',             'Pincho de tortilla extra',  'Otro pincho de tortilla con tu consumición',     10, TRUE),
    ('Mesón do Vilar',          'Pimientos de Padrón',       'Unos pican y otros no',                          30, TRUE),
    ('A Moa',                   'Postre de la casa',         'Tarta de Santiago con helado',                   60, TRUE),
    ('Taberna do Curro',        'Mejillones al vapor',       'Solo los sábados',                               50, TRUE),
    ('Bodeguilla de San Roque', 'Copa de vino a elegir',     'Cualquier vino de la carta por copas',           40, TRUE),
    ('O Dezaseis',              'Cuenco de caldo',           'Caldo gallego de la casa',                       20, TRUE),
    ('Café Casino',             'Chocolate con churros',     'Para una persona',                               50, TRUE),
    ('Horta do Obradoiro',      'Crema de temporada',        'La crema de verduras del día',                   40, TRUE),
    ('Pulpería A Quintana',     'Media de pulpo',            'Media ración de pulpo á feira',                  70, TRUE),
    ('Raxo e Zorza',            'Croquetas caseras',         'Ya no se ofrece: se acabó la temporada',         35, FALSE)
) AS v (business, title, description, cost, active)
JOIN businesses b ON b.name = v.business;

-- Un canje pendiente de maria_tapas, para ver los códigos y la tarea que los caduca (RedemptionExpiryJob)
INSERT INTO redemptions (user_id, reward_id, code, status, redeemed_at)
SELECT u.id, r.id, 'TPC-7K2M9Q', 'PENDING', NOW() - INTERVAL '2 days'
FROM users u, rewards r
WHERE u.username = 'maria_tapas' AND r.title = 'Caldo gallego';

-- Canjes de Adrián: uno pendiente en O Gato Negro (de Hugo) para escanearlo nada más empezar, uno ya usado y
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

-- Más canjes, cada uno para enseñar algo:
--  · TPC-DEMO0004, pendiente en Cervexaría Ensanche (de Hugo): un segundo canje válido para escanear
--  · TPC-DEMO0005, pendiente en Café Casino (de Carmen): Hugo lo escanea y sale "Este canje es de otro local" (403)
--  · TPC-DEMO0006, pendiente desde hace 31 días: la tarea RedemptionExpiryJob lo marca como EXPIRED a los 10 segundos
--    de arrancar (en el log: "1 redemption codes expired")
--  · TPC-DEMO0007, usado en Casa Pepe da Troia: más historial en "Tus canjes"
--  · TPC-DEMO0008, pendiente de lucia_sdc en O Filandón: un segundo cliente (con el portátil) que también canjea en
--    un local de Hugo
INSERT INTO redemptions (user_id, reward_id, code, status, redeemed_at, used_at)
SELECT u.id, r.id, v.code, v.status, NOW() - make_interval(days => v.days_ago),
       CASE WHEN v.status = 'USED' THEN NOW() - make_interval(days => v.days_ago - 1) END
FROM (VALUES
    ('adrianql5', 'Caña de Estrella Galicia', 'TPC-DEMO0004', 'PENDING', 2),
    ('adrianql5', 'Chocolate con churros',    'TPC-DEMO0005', 'PENDING', 1),
    ('adrianql5', 'Tapa de raxo',             'TPC-DEMO0006', 'PENDING', 31),
    ('adrianql5', 'Cunca de ribeiro',         'TPC-DEMO0007', 'USED',    6),
    ('lucia_sdc', 'Vino con tapa doble',      'TPC-DEMO0008', 'PENDING', 1)
) AS v (username, reward, code, status, days_ago)
JOIN users u ON u.username = v.username
JOIN rewards r ON r.title = v.reward;
