-- ============================================================================
--  TapeaCompostela — Datos iniciales
--
--  Los locales y sus direcciones son establecimientos reales de Santiago de
--  Compostela, recogidos de guías gastronómicas y del portal de turismo
--  municipal. VERIFICAD antes de usarlos en producción:
--    * Los horarios son plausibles pero INVENTADOS.
--    * Los precios son orientativos.
--    * Las cartas son representativas del estilo de cada casa, no copias.
--    * Las coordenadas son APROXIMADAS y provisionales: las definitivas las
--      escribe geocodificar.py con los valores que devuelve Nominatim.
--    * osm_type y osm_id van a NULL a propósito. NO se inventan: un id de OSM
--      falso apuntaría a un objeto real equivocado. Los rellena el script.
--  Los usuarios y las valoraciones son ficticios por completo.
--
--  NOMBRES DE CALLE Y OPENSTREETMAP
--  La columna "via" debe coincidir con el tag name= de la calle en OSM. En
--  Santiago están en gallego ("Rúa do Franco", no "Calle del Franco"). Si una
--  dirección no geocodifica, lo primero que hay que revisar es esto: buscad
--  la calle en https://www.openstreetmap.org y copiad el nombre literal.
--
--  Códigos compactos de este esquema:
--    usuarios.sexo    -> 'H' / 'M'
--    consumibles.tipo -> 'T' (tapa) / 'B' (bebida)
--    osm_type         -> 'N' (node) / 'W' (way) / 'R' (relation)
--  Las vistas los devuelven expandidos.
-- ============================================================================


-- ============================================================================
--  PAISES  (nombre_es se envía como parámetro "country" a Nominatim)
-- ============================================================================
INSERT INTO paises (codigo, nombre_gl, nombre_es, nombre_en) VALUES
    ('ES', 'España',         'España',         'Spain'),
    ('PT', 'Portugal',       'Portugal',       'Portugal'),
    ('FR', 'Francia',        'Francia',        'France'),
    ('DE', 'Alemaña',        'Alemania',       'Germany'),
    ('IT', 'Italia',         'Italia',         'Italy'),
    ('IE', 'Irlanda',        'Irlanda',        'Ireland'),
    ('GB', 'Reino Unido',    'Reino Unido',    'United Kingdom'),
    ('NL', 'Países Baixos',  'Países Bajos',   'Netherlands'),
    ('PL', 'Polonia',        'Polonia',        'Poland'),
    ('US', 'Estados Unidos', 'Estados Unidos', 'United States'),
    ('BR', 'Brasil',         'Brasil',         'Brazil'),
    ('KR', 'Corea do Sur',   'Corea del Sur',  'South Korea');


-- ============================================================================
--  LOCALIDADES
--    nombre    -> city    (Nominatim)
--    provincia -> county
--    comunidad -> state
--  Coordenadas aproximadas del centro urbano; las precisas y el bounding box
--  los completa geocodificar.py.
-- ============================================================================
INSERT INTO localidades (id, nombre, provincia, comunidad, pais_codigo, latitud, longitud) VALUES
    ( 1, 'Santiago de Compostela', 'A Coruña',   'Galicia',            'ES', 42.8782,  -8.5448),
    ( 2, 'A Coruña',               'A Coruña',   'Galicia',            'ES', 43.3623,  -8.4115),
    ( 3, 'Lugo',                   'Lugo',       'Galicia',            'ES', 43.0121,  -7.5558),
    ( 4, 'Ourense',                'Ourense',    'Galicia',            'ES', 42.3361,  -7.8641),
    ( 5, 'Pontevedra',             'Pontevedra', 'Galicia',            'ES', 42.4310,  -8.6444),
    ( 6, 'Vigo',                   'Pontevedra', 'Galicia',            'ES', 42.2315,  -8.7126),
    ( 7, 'Ferrol',                 'A Coruña',   'Galicia',            'ES', 43.4844,  -8.2336),
    ( 8, 'Madrid',                 'Madrid',     'Comunidad de Madrid','ES', 40.4168,  -3.7038),
    ( 9, 'Barcelona',              'Barcelona',  'Cataluña',           'ES', 41.3851,   2.1734),
    (10, 'München',                NULL,         'Bayern',             'DE', 48.1351,  11.5820),
    (11, 'Porto',                  NULL,         'Porto',              'PT', 41.1579,  -8.6291),
    (12, 'Dublin',                 NULL,         'Leinster',           'IE', 53.3498,  -6.2603),
    (13, 'Lyon',                   NULL,         'Auvergne-Rhône-Alpes','FR',45.7640,   4.8357),
    (14, 'Seoul',                  NULL,         NULL,                 'KR', 37.5665, 126.9780);


-- ============================================================================
--  ALERGENOS — los 14 del Reglamento (UE) 1169/2011
-- ============================================================================
INSERT INTO alergenos (id, codigo, nombre) VALUES
    ( 1, 'GLUTEN',        'Cereales con gluten'),
    ( 2, 'CRUSTACEOS',    'Crustáceos'),
    ( 3, 'HUEVOS',        'Huevos'),
    ( 4, 'PESCADO',       'Pescado'),
    ( 5, 'CACAHUETES',    'Cacahuetes'),
    ( 6, 'SOJA',          'Soja'),
    ( 7, 'LACTEOS',       'Leche y derivados'),
    ( 8, 'FRUTOS_CASCARA','Frutos de cáscara'),
    ( 9, 'APIO',          'Apio'),
    (10, 'MOSTAZA',       'Mostaza'),
    (11, 'SESAMO',        'Granos de sésamo'),
    (12, 'SULFITOS',      'Dióxido de azufre y sulfitos'),
    (13, 'ALTRAMUCES',    'Altramuces'),
    (14, 'MOLUSCOS',      'Moluscos');


-- ============================================================================
--  LOCALES  (todos en Santiago de Compostela, localidad_id = 1)
--
--  "via" en gallego, tal como aparece en OpenStreetMap.
--  "numero" no numérico ("s/n", "13-18") se geocodifica a nivel de calle:
--  la vista v_locales_nominatim ya lo contempla.
--  osm_type / osm_id / bbox / display_name -> los rellena geocodificar.py
-- ============================================================================
INSERT INTO locales (id, nombre, via, numero, codigo_postal, localidad_id, latitud, longitud, foto_perfil) VALUES
    ( 1, 'Taberna O Gato Negro',     'Rúa da Raíña',             's/n',   '15702', 1, 42.8795, -8.5458, '/img/locales/gato-negro.jpg'),
    ( 2, 'Bar Orella',               'Rúa da Raíña',             '21',    '15702', 1, 42.8793, -8.5456, '/img/locales/orella.jpg'),
    ( 3, 'Viñoteca Ventosela',       'Rúa da Raíña',             '28',    '15702', 1, 42.8791, -8.5455, '/img/locales/ventosela.jpg'),
    ( 4, 'A Taberna do Bispo',       'Rúa do Franco',            '37',    '15702', 1, 42.8788, -8.5466, '/img/locales/taberna-bispo.jpg'),
    ( 5, 'Petiscos do Cardeal',      'Rúa do Franco',            NULL,    '15702', 1, 42.8790, -8.5464, '/img/locales/petiscos-cardeal.jpg'),
    ( 6, 'Mesón O 42',               'Rúa do Franco',            '42',    '15702', 1, 42.8786, -8.5468, '/img/locales/o42.jpg'),
    ( 7, 'Bar Los Amigos',           'Rúa do Franco',            '12',    '15702', 1, 42.8801, -8.5459, '/img/locales/los-amigos.jpg'),
    ( 8, 'Bar La Tita',              'Rúa Nova',                 '46',    '15705', 1, 42.8794, -8.5450, '/img/locales/la-tita.jpg'),
    ( 9, 'A Gamela',                 'Rúa da Oliveira',          '5',     '15703', 1, 42.8801, -8.5428, '/img/locales/a-gamela.jpg'),
    (10, 'A Sucursal de Moha',       'Praza de Santo Agostiño',  '7',     '15703', 1, 42.8800, -8.5432, '/img/locales/sucursal-moha.jpg'),
    (11, 'A Tasquiña de San Pedro',  'Rúa da Cruz de San Pedro', '2',     '15703', 1, 42.8818, -8.5375, '/img/locales/tasquina-san-pedro.jpg'),
    (12, 'Restaurante San Clemente', 'Rúa de San Clemente',      '6',     '15705', 1, 42.8800, -8.5490, '/img/locales/san-clemente.jpg'),
    (13, 'Cabalo Branco',            'Praza da Pescadería Vella','5',     '15703', 1, 42.8797, -8.5434, '/img/locales/cabalo-branco.jpg'),
    (14, 'Abastos 2.0',              'Rúa das Ameas',            '13-18', '15703', 1, 42.8781, -8.5427, '/img/locales/abastos.jpg');


-- ============================================================================
--  HORARIOS  (1 = lunes ... 7 = domingo; sin fila = cerrado)
-- ============================================================================
INSERT INTO horarios (local_id, dia_semana, hora_apertura, hora_cierre) VALUES
    -- O Gato Negro: cerrado los domingos, jornada partida
    (1, 1, '11:00', '16:00'), (1, 1, '19:00', '23:30'),
    (1, 2, '11:00', '16:00'), (1, 2, '19:00', '23:30'),
    (1, 3, '11:00', '16:00'), (1, 3, '19:00', '23:30'),
    (1, 4, '11:00', '16:00'), (1, 4, '19:00', '23:30'),
    (1, 5, '11:00', '16:00'), (1, 5, '19:00', '00:30'),
    (1, 6, '11:00', '00:30'),
    -- Bar Orella: todos los días, horario continuo
    (2, 1, '12:00', '00:00'), (2, 2, '12:00', '00:00'), (2, 3, '12:00', '00:00'),
    (2, 4, '12:00', '00:00'), (2, 5, '12:00', '01:30'), (2, 6, '12:00', '01:30'),
    (2, 7, '12:00', '00:00'),
    -- Viñoteca Ventosela: abre por la tarde
    (3, 2, '18:00', '00:00'), (3, 3, '18:00', '00:00'), (3, 4, '18:00', '00:00'),
    (3, 5, '18:00', '01:30'), (3, 6, '12:30', '01:30'), (3, 7, '12:30', '23:00'),
    -- A Taberna do Bispo
    (4, 1, '11:30', '16:00'), (4, 1, '19:30', '23:30'),
    (4, 3, '11:30', '16:00'), (4, 3, '19:30', '23:30'),
    (4, 4, '11:30', '16:00'), (4, 4, '19:30', '23:30'),
    (4, 5, '11:30', '00:30'), (4, 6, '11:30', '00:30'), (4, 7, '11:30', '23:00'),
    -- Petiscos do Cardeal
    (5, 1, '12:00', '23:30'), (5, 2, '12:00', '23:30'), (5, 3, '12:00', '23:30'),
    (5, 4, '12:00', '23:30'), (5, 5, '12:00', '01:00'), (5, 6, '12:00', '01:00'),
    (5, 7, '12:00', '23:00'),
    -- Mesón O 42
    (6, 1, '12:00', '16:30'), (6, 1, '19:00', '23:30'),
    (6, 2, '12:00', '16:30'), (6, 2, '19:00', '23:30'),
    (6, 3, '12:00', '16:30'), (6, 3, '19:00', '23:30'),
    (6, 4, '12:00', '16:30'), (6, 4, '19:00', '23:30'),
    (6, 5, '12:00', '00:30'), (6, 6, '12:00', '00:30'), (6, 7, '12:00', '17:00'),
    -- Bar Los Amigos
    (7, 1, '12:00', '00:00'), (7, 2, '12:00', '00:00'), (7, 3, '12:00', '00:00'),
    (7, 4, '12:00', '00:00'), (7, 5, '12:00', '00:00'), (7, 6, '12:00', '00:00'),
    (7, 7, '12:00', '00:00'),
    -- Bar La Tita: cierra los lunes
    (8, 2, '10:00', '23:00'), (8, 3, '10:00', '23:00'), (8, 4, '10:00', '23:00'),
    (8, 5, '10:00', '00:00'), (8, 6, '10:00', '00:00'), (8, 7, '10:00', '22:00'),
    -- A Gamela
    (9, 2, '12:30', '16:00'), (9, 2, '20:00', '23:30'),
    (9, 3, '12:30', '16:00'), (9, 3, '20:00', '23:30'),
    (9, 4, '12:30', '16:00'), (9, 4, '20:00', '23:30'),
    (9, 5, '12:30', '00:00'), (9, 6, '12:30', '00:00'),
    -- A Sucursal de Moha
    (10, 1, '11:00', '23:00'), (10, 2, '11:00', '23:00'), (10, 3, '11:00', '23:00'),
    (10, 4, '11:00', '23:00'), (10, 5, '11:00', '00:30'), (10, 6, '11:00', '00:30'),
    -- A Tasquiña de San Pedro
    (11, 3, '18:30', '00:00'), (11, 4, '18:30', '00:00'),
    (11, 5, '18:30', '01:30'), (11, 6, '12:30', '01:30'), (11, 7, '12:30', '23:00'),
    -- Restaurante San Clemente
    (12, 1, '12:00', '00:00'), (12, 2, '12:00', '00:00'), (12, 3, '12:00', '00:00'),
    (12, 4, '12:00', '00:00'), (12, 5, '12:00', '01:00'), (12, 6, '12:00', '01:00'),
    (12, 7, '12:00', '00:00'),
    -- Cabalo Branco
    (13, 1, '12:00', '23:30'), (13, 2, '12:00', '23:30'), (13, 3, '12:00', '23:30'),
    (13, 4, '12:00', '23:30'), (13, 5, '12:00', '00:30'), (13, 6, '12:00', '00:30'),
    (13, 7, '12:00', '23:00'),
    -- Abastos 2.0: ligado al mercado, cierra domingo y lunes
    (14, 2, '12:00', '16:00'), (14, 3, '12:00', '16:00'),
    (14, 4, '12:00', '16:00'), (14, 4, '20:00', '23:00'),
    (14, 5, '12:00', '16:00'), (14, 5, '20:00', '23:00'),
    (14, 6, '12:00', '16:30'), (14, 6, '20:00', '23:00');


-- ============================================================================
--  CONSUMIBLES   (tipo 'T' = tapa, 'B' = bebida; precio 0.00 = tapa incluida)
-- ============================================================================
INSERT INTO consumibles (id, local_id, tipo, precio, nombre, descripcion, imagen) VALUES
    -- O Gato Negro
    ( 1,  1, 'T',  3.50, 'Empanada de congro',        'Empanada casera de congrio con cebolla pochada',              '/img/tapas/empanada-congro.jpg'),
    ( 2,  1, 'T',  9.00, 'Sardiñas asadas',           'Ración de sardinas a la plancha con pan de maíz',             '/img/tapas/sardinas.jpg'),
    ( 3,  1, 'T',  7.50, 'Fígado encebolado',         'Hígado de ternera encebollado, receta de la casa',            '/img/tapas/figado.jpg'),
    ( 4,  1, 'B',  1.80, 'Ribeiro en cunca',          'Vino de Ribeiro servido en cuenco de cerámica',               '/img/bebidas/ribeiro-cunca.jpg'),
    -- Bar Orella
    ( 5,  2, 'T',  0.00, 'Orella cocida',             'La tapa de la casa: oreja de cerdo cocida con pimentón',      '/img/tapas/orella.jpg'),
    ( 6,  2, 'T',  6.50, 'Pementos de Padrón',        'Pimientos de Padrón fritos con sal gruesa',                   '/img/tapas/pementos.jpg'),
    ( 7,  2, 'B',  1.80, 'Caña Estrella Galicia',     'Caña de barril bien tirada',                                  '/img/bebidas/cana.jpg'),
    ( 8,  2, 'T',  8.00, 'Zorza con patacas',         'Carne de cerdo adobada con patatas fritas',                   '/img/tapas/zorza.jpg'),
    -- Viñoteca Ventosela
    ( 9,  3, 'T',  0.00, 'Tabla de embutidos',        'Tapa incluida: surtido de embutidos de la tierra',            '/img/tapas/embutidos.jpg'),
    (10,  3, 'T',  4.50, 'Tosta de chicharróns',      'Tosta de chicharrones con queso de Arzúa-Ulloa fundido',      '/img/tapas/tosta-chicharrons.jpg'),
    (11,  3, 'B',  2.50, 'Copa de viño de Betanzos',  'Blanco de la Tierra de Betanzos, fresco y ligero',            '/img/bebidas/betanzos.jpg'),
    (12,  3, 'B',  3.00, 'Copa de Albariño',          'Albariño de Rías Baixas por copas',                           '/img/bebidas/albarino.jpg'),
    -- A Taberna do Bispo
    (13,  4, 'T', 12.00, 'Zamburiñas á prancha',      'Zamburiñas a la plancha con ajo y perejil',                   '/img/tapas/zamburinas.jpg'),
    (14,  4, 'T',  7.00, 'Croquetas de marisco',      'Croquetas cremosas de marisco hechas a diario',               '/img/tapas/croquetas-marisco.jpg'),
    (15,  4, 'T',  9.50, 'Anchoas con tomate',        'Anchoas del Cantábrico sobre ensalada de tomate',             '/img/tapas/anchoas.jpg'),
    (16,  4, 'B',  2.60, '1906 Reserva Especial',     'Cerveza de la casa Estrella Galicia, tirada en copa',         '/img/bebidas/1906.jpg'),
    -- Petiscos do Cardeal
    (17,  5, 'T',  5.50, 'Pemento recheo de bacallau','Pimiento del piquillo relleno de bacalao',                    '/img/tapas/pemento-recheo.jpg'),
    (18,  5, 'T',  3.00, 'Tortilla de patacas',       'Pincho de tortilla jugosa',                                   '/img/tapas/tortilla.jpg'),
    (19,  5, 'T',  6.00, 'Croquetas caseiras',        'Croquetas de jamón hechas en casa',                           '/img/tapas/croquetas.jpg'),
    (20,  5, 'B',  1.70, 'Caña',                      'Caña de cerveza',                                             '/img/bebidas/cana2.jpg'),
    -- Mesón O 42
    (21,  6, 'T', 14.00, 'Pulpo á feira',             'Pulpo cocido con pimentón, sal gruesa y aceite de oliva',     '/img/tapas/pulpo.jpg'),
    (22,  6, 'T',  8.50, 'Mexillóns ao vapor',        'Mejillones de la ría al vapor con limón',                     '/img/tapas/mexillons.jpg'),
    (23,  6, 'B',  7.00, 'Xarra de Ribeiro',          'Jarra de Ribeiro de la casa',                                 '/img/bebidas/xarra-ribeiro.jpg'),
    (24,  6, 'T',  0.00, 'Caldo galego',              'Tapa incluida: caldo con grelos, patata y unto',              '/img/tapas/caldo.jpg'),
    -- Bar Los Amigos
    (25,  7, 'T', 13.50, 'Pulpo á galega',            'Pulpo con cachelos, pimentón dulce y picante',                '/img/tapas/pulpo-galega.jpg'),
    (26,  7, 'T',  7.50, 'Tacos de tenreira',         'Tacos de ternera gallega con salsa de la casa',               '/img/tapas/tacos.jpg'),
    (27,  7, 'T',  6.00, 'Croquetas caseiras',        'Croquetas artesanas con ingredientes locales',                '/img/tapas/croquetas2.jpg'),
    -- Bar La Tita
    (28,  8, 'T',  2.80, 'Pincho de tortilla',        'El pincho de tortilla más famoso de Santiago',                '/img/tapas/pincho-tortilla.jpg'),
    (29,  8, 'T',  8.50, 'Ración de tortilla',        'Media tortilla para compartir',                               '/img/tapas/racion-tortilla.jpg'),
    (30,  8, 'B',  2.00, 'Copa de viño da casa',      'Tinto o blanco de la casa',                                   '/img/bebidas/vino-casa.jpg'),
    -- A Gamela
    (31,  9, 'T',  9.00, 'Ración de setas',           'Setas de temporada salteadas con ajo',                        '/img/tapas/setas.jpg'),
    (32,  9, 'T', 10.00, 'Embutidos leoneses',        'Tabla de cecina, chorizo y salchichón de León',               '/img/tapas/embutidos-leon.jpg'),
    (33,  9, 'B',  1.90, 'Caña con tapa',             'Caña acompañada de la tapa del día',                          '/img/bebidas/cana3.jpg'),
    -- A Sucursal de Moha
    (34, 10, 'T',  0.00, 'Pincho de tortilla',        'Tapa incluida: tortilla jugosa, de las mejores de la ciudad', '/img/tapas/tortilla-moha.jpg'),
    (35, 10, 'T', 11.00, 'Chipiróns á prancha',       'Chipirones a la plancha con cebolla caramelizada',            '/img/tapas/chipirons.jpg'),
    (36, 10, 'B',  1.80, 'Estrella Galicia',          'Caña de Estrella Galicia',                                    '/img/bebidas/estrella.jpg'),
    -- A Tasquiña de San Pedro
    (37, 11, 'T',  0.00, 'Lentellas',                 'Tapa incluida: lentejas guisadas con chorizo',                '/img/tapas/lentellas.jpg'),
    (38, 11, 'T',  0.00, 'Patacas ali oli',           'Tapa incluida: patatas con alioli casero',                    '/img/tapas/alioli.jpg'),
    (39, 11, 'T',  7.50, 'Callos',                    'Callos a la gallega con garbanzos',                           '/img/tapas/callos.jpg'),
    -- Restaurante San Clemente
    (40, 12, 'T',  0.00, 'Tapa de cociña do día',     'Tapa incluida, el camarero pregunta cuál prefieres',          '/img/tapas/tapa-dia.jpg'),
    (41, 12, 'T',  9.50, 'Raxo con patacas',          'Lomo de cerdo adobado con patatas',                           '/img/tapas/raxo.jpg'),
    (42, 12, 'B',  2.00, 'Caña',                      'Caña de cerveza en terraza',                                  '/img/bebidas/cana4.jpg'),
    -- Cabalo Branco
    (43, 13, 'T', 12.50, 'Navallas á prancha',        'Navajas a la plancha con limón',                              '/img/tapas/navallas.jpg'),
    (44, 13, 'T',  4.00, 'Empanada de zamburiñas',    'Porción de empanada de zamburiñas',                           '/img/tapas/empanada-zamb.jpg'),
    (45, 13, 'B',  6.50, 'Xarra de Ribeiro',          'Jarra de Ribeiro para la mesa',                               '/img/bebidas/xarra2.jpg'),
    -- Abastos 2.0
    (46, 14, 'T',  3.50, 'Ostra da Ría',              'Ostra abierta al momento, producto del mercado',              '/img/tapas/ostra.jpg'),
    (47, 14, 'T',  6.00, 'Bocadillo de xoubas',       'Bocadillo de sardinillas marinadas',                          '/img/tapas/xoubas.jpg'),
    (48, 14, 'B',  3.20, 'Copa de Albariño',          'Albariño seleccionado, la referencia cambia cada semana',     '/img/bebidas/albarino2.jpg');


-- ============================================================================
--  ALERGENOS DE CADA CONSUMIBLE
-- ============================================================================
INSERT INTO consumible_alergenos (consumible_id, alergeno_id) VALUES
    ( 1,  1), ( 1,  3), ( 1,  4),                      -- empanada de congrio
    ( 2,  4),                                          -- sardinas
    ( 3,  9),                                          -- hígado encebollado
    ( 4, 12),                                          -- ribeiro
    ( 6, 12),                                          -- pimientos
    ( 7,  1),                                          -- cerveza
    ( 8, 12),                                          -- zorza
    ( 9,  7), ( 9, 12),                                -- embutidos
    (10,  1), (10,  7),                                -- tosta con queso
    (11, 12), (12, 12),                                -- vinos
    (13, 14),                                          -- zamburiñas
    (14,  1), (14,  2), (14,  7), (14, 14),            -- croquetas de marisco
    (15,  4),                                          -- anchoas
    (16,  1),                                          -- cerveza
    (17,  1), (17,  4), (17,  7),                      -- pimiento relleno de bacalao
    (18,  3),                                          -- tortilla
    (19,  1), (19,  3), (19,  7),                      -- croquetas de jamón
    (20,  1),                                          -- caña
    (21, 14),                                          -- pulpo
    (22, 14),                                          -- mejillones
    (23, 12),                                          -- ribeiro
    (24,  9),                                          -- caldo gallego
    (25, 14),                                          -- pulpo
    (27,  1), (27,  3), (27,  7),                      -- croquetas
    (28,  3), (29,  3),                                -- tortillas
    (30, 12),                                          -- vino
    (32, 12),                                          -- embutidos
    (33,  1),                                          -- caña
    (34,  3),                                          -- tortilla
    (35, 14),                                          -- chipirones
    (36,  1),                                          -- caña
    (37,  9), (37, 12),                                -- lentejas con chorizo
    (38,  3),                                          -- alioli
    (39,  9),                                          -- callos
    (41, 12),                                          -- raxo
    (42,  1),                                          -- caña
    (43, 14),                                          -- navajas
    (44,  1), (44,  3), (44, 14),                      -- empanada de zamburiñas
    (45, 12),                                          -- ribeiro
    (46, 14),                                          -- ostra
    (47,  1), (47,  4),                                -- bocadillo de sardinillas
    (48, 12);                                          -- albariño


-- ============================================================================
--  USUARIOS   (sexo: 'H' = hombre, 'M' = mujer)
--  Se insertan con puntos = 0: el trigger los acumula al insertar las
--  valoraciones, y al final se añade un bonus de actividad histórica.
--  marcos_erasmus va sin residencia: atributo opcional sin informar.
-- ============================================================================
INSERT INTO usuarios (id, nombre_usuario, nombre, apellido1, apellido2, fecha_nacimiento, sexo,
                      residencia_id, foto_perfil, cancion_titulo, cancion_artista, cancion_url) VALUES
    ( 1, 'tapeadora_maior',   'Antía',   'Vilar',    'Souto',    '1999-03-14', 'M',    1,
         '/img/perfiles/antia.jpg',   'Figa',                 'Tanxugueiras',       'https://open.spotify.com/'),
    ( 2, 'xancino',           'Xan',     'Castro',   'Ferro',    '1996-07-02', 'H',    1,
         '/img/perfiles/xan.jpg',     'Miña terra galega',    'Siniestro Total',    NULL),
    ( 3, 'peregrina_famenta', 'Laura',   'Méndez',   'Ruiz',     '2001-11-23', 'M',    4,
         NULL,                        'Memoria da noite',     'Luar na Lubre',      NULL),
    ( 4, 'breogan87',         'Breogán', 'Lois',     'Pereira',  '1987-01-30', 'H',    3,
         '/img/perfiles/breogan.jpg', 'Galicia caníbal',      'Os Resentidos',      NULL),
    ( 5, 'uxia_p',            'Uxía',    'Pardo',    'Nogueira', '2003-05-09', 'M',    1,
         '/img/perfiles/uxia.jpg',    'Veleno',               'Baiuca',             NULL),
    ( 6, 'marcos_erasmus',    'Marcos',  'Ledo',     'Iglesias', '2000-09-17', 'H', NULL,
         NULL,                        'Chegou o tempo',       'Heredeiros da Crus', NULL),
    ( 7, 'noagz',             'Noa',     'González', 'Caamaño',  '1998-12-05', 'M',    2,
         '/img/perfiles/noa.jpg',     'Admírote paisaxe',     'Sés',                NULL),
    ( 8, 'o_paporrubio',      'Iago',    'Rial',     'Barreiro', '1994-04-21', 'H',    5,
         NULL,                        'Muiñeira de Chantada', 'Carlos Núñez',       NULL),
    ( 9, 'sabelaa',           'Sabela',  'Torres',   'Quintás',  '2002-02-11', 'M',    1,
         '/img/perfiles/sabela.jpg',  'Negra sombra',         'Luar na Lubre',      NULL),
    (10, 'hans_camino',       'Hans',    'Müller',   NULL,       '1979-08-03', 'H',   10,
         '/img/perfiles/hans.jpg',    'A viaxe',              'Xabier Díaz',        NULL),
    (11, 'joana_porto',       'Joana',   'Ribeiro',  NULL,       '1995-06-27', 'M',   11,
         NULL,                        'Grândola vila morena', 'Zeca Afonso',        NULL),
    (12, 'seanoc',            'Sean',    'O Connor', NULL,       '1991-10-14', 'H',   12,
         '/img/perfiles/sean.jpg',    'The Auld Triangle',    'The Dubliners',      NULL);


-- ============================================================================
--  VALORACIONES
--  consumible_id NULL -> valoración del local; si no, de una tapa concreta
--  (que debe pertenecer a ese local: lo impone la FK compuesta).
-- ============================================================================
INSERT INTO valoraciones (usuario_id, local_id, consumible_id, puntuacion, texto, foto, fecha) VALUES
    ( 1,  1, NULL, 5, 'Taberna de las de siempre. Sin postureo, con el mostrador lleno y la gente de pie.', '/img/valoraciones/v001.jpg', '2026-01-12 21:34:00+01'),
    ( 4,  1,    1, 5, 'La empanada de congrio está a otro nivel. Masa fina y relleno jugoso.',               NULL,                        '2026-02-03 14:10:00+01'),
    ( 7,  1,    2, 4, 'Las sardinas muy buenas, aunque el sitio se llena mucho a partir de las nueve.',      NULL,                        '2026-02-28 22:05:00+01'),
    (12,  1, NULL, 5, 'Best place I found in the old town. No English menu and that is a good sign.',        NULL,                        '2026-06-08 21:00:00+02'),
    ( 1,  2,    5, 5, 'Vas por la oreja y te quedas por el ambiente. La tapa incluida es generosa.',         '/img/valoraciones/v005.jpg', '2026-01-20 20:15:00+01'),
    ( 2,  2, NULL, 4, 'Clásico de la Raíña. Precio justo y trato cercano.',                                  NULL,                        '2026-03-05 21:00:00+01'),
    ( 5,  2,    6, 3, 'Los pimientos estaban ricos pero me tocó la tanda de los que pican.',                 NULL,                        '2026-03-18 20:40:00+01'),
    (11,  2, NULL, 4, 'Muito parecido às tascas do Porto, e isso é um elogio.',                              NULL,                        '2026-05-11 20:30:00+02'),
    ( 3,  3,   10, 5, 'La tosta de chicharrones con queso de Arzúa es la mejor cosa que probé en el Camino.','/img/valoraciones/v009.jpg', '2026-04-02 19:50:00+02'),
    ( 8,  3,   11, 4, 'Buena selección de vinos pequeños. El de Betanzos merece la pena.',                   NULL,                        '2026-04-11 20:20:00+02'),
    ( 9,  3, NULL, 5, 'Sitio tranquilo para hablar sin gritar. Raro y valioso en esta zona.',                NULL,                        '2026-05-06 19:30:00+02'),
    ( 1,  4,   13, 5, 'Zamburiñas impecables. Es de lo poco del Franco que no es trampa para turistas.',     '/img/valoraciones/v012.jpg', '2026-02-14 21:45:00+01'),
    ( 6,  4, NULL, 4, 'Barra moderna, buen servicio y hablan inglés, que para mis amigos fue clave.',        NULL,                        '2026-03-22 22:10:00+01'),
    (10,  4,   15, 4, 'Anchoas buenísimas, aunque el precio ya no es de tapeo.',                             NULL,                        '2026-05-19 20:00:00+02'),
    ( 5,  5,   18, 4, 'La tortilla bien cuajada, como debe ser.',                                            NULL,                        '2026-01-28 14:25:00+01'),
    ( 7,  5, NULL, 4, 'Relación calidad-precio muy buena para estar donde está.',                            '/img/valoraciones/v016.jpg', '2026-04-27 21:15:00+02'),
    ( 2,  6,   21, 5, 'El pulpo, en su punto. Ni chicloso ni deshecho.',                                     '/img/valoraciones/v017.jpg', '2026-02-07 15:00:00+01'),
    ( 4,  6, NULL, 5, 'Parece un bar de pueblo metido en el Franco. Precios de antes.',                      NULL,                        '2026-03-14 14:45:00+01'),
    ( 8,  6,   23, 4, 'La jarra de Ribeiro cunde para tres. Pedid comida antes.',                            NULL,                        '2026-06-02 21:30:00+02'),
    (12,  6, NULL, 5, 'Cheap, busy and honest. Came back three nights in a row.',                            NULL,                        '2026-06-11 21:50:00+02'),
    ( 3,  7, NULL, 3, 'Está bien pero se nota muy enfocado a peregrinos. Las croquetas sí, muy ricas.',      NULL,                        '2026-04-05 13:50:00+02'),
    ( 9,  7,   25, 4, 'Pulpo correcto y ración abundante.',                                                  NULL,                        '2026-05-23 14:30:00+02'),
    ( 1,  8,   28, 5, 'La tortilla de La Tita es una institución. Hay cola por algo.',                       '/img/valoraciones/v023.jpg', '2026-01-09 13:20:00+01'),
    ( 5,  8, NULL, 4, 'Pequeño y siempre lleno. Ve fuera de hora punta.',                                    NULL,                        '2026-02-21 13:05:00+01'),
    ( 6,  8,   29, 5, 'Pedimos ración para compartir y sobró. Diez euros bien gastados.',                    NULL,                        '2026-04-18 14:00:00+02'),
    (11,  8,   28, 4, 'A tortilha é boa, mas a fila às duas da tarde é desanimadora.',                       NULL,                        '2026-05-14 14:20:00+02'),
    ( 2,  9,   31, 4, 'Las setas de temporada estaban muy buenas, con mucho ajo.',                           NULL,                        '2026-03-01 21:20:00+01'),
    ( 4,  9, NULL, 4, 'Clásico fuera de la zona turística. Los de casa vienen aquí.',                        NULL,                        '2026-05-30 21:40:00+02'),
    ( 7, 10,   34, 5, 'Hay quien dice que la tortilla es mejor que la de La Tita. Yo no lo desmiento.',      '/img/valoraciones/v029.jpg', '2026-02-11 20:50:00+01'),
    ( 9, 10,   35, 4, 'Chipirones muy correctos y mesas fuera cuando hace bueno.',                           NULL,                        '2026-06-14 21:10:00+02'),
    ( 1, 11, NULL, 5, 'Tapa casera de verdad: un día lentejas, otro callos. Nunca sabes qué te toca.',       NULL,                        '2026-03-28 20:30:00+01'),
    ( 8, 11,   39, 4, 'Los callos, potentes. No es tapa de picar, es casi cena.',                            NULL,                        '2026-04-25 21:00:00+02'),
    ( 3, 12, NULL, 4, 'Terraza muy concurrida, cuesta coger sitio, pero te preguntan qué tapa quieres.',     '/img/valoraciones/v033.jpg', '2026-05-09 14:15:00+02'),
    (10, 12,   41, 3, 'El raxo correcto, nada memorable. El sitio vale por la terraza.',                     NULL,                        '2026-06-20 14:40:00+02'),
    ( 6, 13, NULL, 3, 'Raciones abundantes pero la calidad no llega a la de otros de la zona.',              NULL,                        '2026-05-02 21:25:00+02'),
    ( 2, 13,   43, 4, 'Las navajas, frescas. El resto de la carta ya no tanto.',                             NULL,                        '2026-06-27 20:55:00+02'),
    ( 1, 14,   46, 5, 'Ostra abierta delante de ti con el mercado al lado. Difícil de superar.',             '/img/valoraciones/v037.jpg', '2026-04-10 13:30:00+02'),
    ( 4, 14, NULL, 5, 'Sin carta fija: cocinan lo que encuentran bueno esa mañana. Reservad.',               NULL,                        '2026-05-16 13:45:00+02'),
    ( 9, 14,   48, 4, 'Cambian el albariño por copas cada semana, un acierto.',                              NULL,                        '2026-06-05 20:15:00+02'),
    (10, 14, NULL, 5, 'Nach dem Camino das beste Essen der ganzen Reise.',                                   NULL,                        '2026-06-22 13:20:00+02');


-- ============================================================================
--  BONUS DE PUNTOS (actividad histórica simulada)
-- ============================================================================
UPDATE usuarios SET puntos = puntos + 285 WHERE nombre_usuario = 'tapeadora_maior';
UPDATE usuarios SET puntos = puntos + 140 WHERE nombre_usuario = 'xancino';
UPDATE usuarios SET puntos = puntos +  55 WHERE nombre_usuario = 'peregrina_famenta';
UPDATE usuarios SET puntos = puntos + 460 WHERE nombre_usuario = 'breogan87';
UPDATE usuarios SET puntos = puntos +  30 WHERE nombre_usuario = 'uxia_p';
UPDATE usuarios SET puntos = puntos + 175 WHERE nombre_usuario = 'marcos_erasmus';
UPDATE usuarios SET puntos = puntos + 230 WHERE nombre_usuario = 'noagz';
UPDATE usuarios SET puntos = puntos + 380 WHERE nombre_usuario = 'o_paporrubio';
UPDATE usuarios SET puntos = puntos + 105 WHERE nombre_usuario = 'sabelaa';
UPDATE usuarios SET puntos = puntos +  20 WHERE nombre_usuario = 'hans_camino';
UPDATE usuarios SET puntos = puntos +  65 WHERE nombre_usuario = 'joana_porto';
UPDATE usuarios SET puntos = puntos +  90 WHERE nombre_usuario = 'seanoc';


-- ============================================================================
--  REAJUSTE DE SECUENCIAS
--  Imprescindible tras insertar ids explícitos en columnas IDENTITY.
--  (paises no lleva secuencia: su PK es el código ISO.)
-- ============================================================================
SELECT setval(pg_get_serial_sequence('localidades', 'id'), (SELECT COALESCE(MAX(id), 1) FROM localidades));
SELECT setval(pg_get_serial_sequence('usuarios',    'id'), (SELECT COALESCE(MAX(id), 1) FROM usuarios));
SELECT setval(pg_get_serial_sequence('locales',     'id'), (SELECT COALESCE(MAX(id), 1) FROM locales));
SELECT setval(pg_get_serial_sequence('horarios',    'id'), (SELECT COALESCE(MAX(id), 1) FROM horarios));
SELECT setval(pg_get_serial_sequence('consumibles', 'id'), (SELECT COALESCE(MAX(id), 1) FROM consumibles));
SELECT setval(pg_get_serial_sequence('alergenos',   'id'), (SELECT COALESCE(MAX(id), 1) FROM alergenos));
SELECT setval(pg_get_serial_sequence('valoraciones','id'), (SELECT COALESCE(MAX(id), 1) FROM valoraciones));


-- ============================================================================
--  SIGUIENTE PASO: GEOCODIFICAR
--
--  Ejecutad geocodificar.py para que Nominatim rellene osm_type, osm_id,
--  coordenadas exactas, bounding box y display_name. El script respeta el
--  límite de 1 petición por segundo de la política de uso:
--      https://operations.osmfoundation.org/policies/nominatim/
--
--  Comprobar qué queda pendiente:
--    SELECT * FROM v_locales_nominatim WHERE geocodificado_en IS NULL;
--
--  Ver el resultado sobre el mapa:
--    SELECT nombre, latitud, longitud, osm_url FROM v_locales_mapa;
--
--  RECORDATORIO: la interfaz que muestre el mapa DEBE incluir la atribución
--  "© OpenStreetMap contributors".
-- ============================================================================