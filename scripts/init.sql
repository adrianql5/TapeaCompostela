-- ============================================================================
--  TapeaCompostela — Esquema de base de datos
--  PostgreSQL 18
--
--  ---------------------------------------------------------------------------
--  INTEGRACIÓN CON OPENSTREETMAP / NOMINATIM
--  ---------------------------------------------------------------------------
--  POLÍTICA DE USO OBLIGATORIA:
--      https://operations.osmfoundation.org/policies/nominatim/
--  Resumen de lo que afecta a este esquema:
--    * Máximo 1 petición por segundo, contando la suma de TODOS los usuarios
--      de la aplicación, no por usuario.
--    * Scripts que corren más de un día o a intervalos regulares: 4 peticiones
--      por minuto.
--    * Los resultados DEBEN cachearse. Repetir la misma consulta puede
--      provocar el bloqueo de la aplicación.
--    * User-Agent o Referer propio que identifique la aplicación.
--    * Atribución visible: "© OpenStreetMap contributors". Licencia ODbL.
--
--  Por eso el esquema guarda el resultado de la geocodificación en lugar de
--  llamar a Nominatim en cada petición: la caché no es una optimización, es
--  un requisito de la política.
--
--  La dirección se descompone en los campos de la consulta estructurada de
--  Nominatim (/search):
--      street     -> numero + via  (Nominatim documenta "housenumber street")
--      city       -> localidades.nombre
--      county     -> localidades.provincia
--      state      -> localidades.comunidad
--      country    -> paises.nombre_es
--      postalcode -> locales.codigo_postal
--  La vista v_locales_nominatim los devuelve ya montados.
--
--  La referencia estable de un objeto OSM es la pareja (osm_type, osm_id):
--  osm_type es 'N' (node), 'W' (way) o 'R' (relation). NO se guarda place_id,
--  que es volátil y depende del servidor.
--
--  ---------------------------------------------------------------------------
--  OPTIMIZACIONES DE ALMACENAMIENTO APLICADAS
--  ---------------------------------------------------------------------------
--  1. ORDEN DE COLUMNAS POR ALINEACIÓN: primero las de 8 bytes, luego 4, luego
--     2, y al final las de longitud variable. Elimina el relleno que PostgreSQL
--     inserta entre columnas. Ahorro gratuito: no cuesta ni un JOIN.
--     Por eso "id" no siempre está en la primera línea.
--  2. CLAVES PRIMARIAS INTEGER en vez de BIGINT: 4 bytes en vez de 8, y el
--     ahorro se multiplica en cada FK. Excepción justificada: osm_id, que SÍ
--     necesita BIGINT porque los identificadores de nodo de OSM superan de
--     largo los 2.147 millones.
--  3. DOMINIOS CERRADOS COMO CHAR(1): sexo ('H'/'M'), tipo ('T'/'B'),
--     osm_type ('N'/'W'/'R'). Las vistas los devuelven expandidos.
--  4. COORDENADAS COMO DOUBLE PRECISION: 8 bytes fijos frente a los ~10
--     variables de NUMERIC(9,6), con precisión de sobra.
--  5. CLAVE PRIMARIA NATURAL en paises: el código ISO 3166-1 alfa-2.
--
--  DESCARTADAS, y por qué:
--    * precio como INTEGER de céntimos: NUMERIC es el tipo correcto para
--      dinero y la claridad vale más que 4 bytes.
--    * sexo en tabla de catálogo: un SMALLINT ocupa lo mismo que CHAR(1) y
--      además obliga a un JOIN. El catálogo se justifica cuando el dominio
--      crece o tiene atributos propios, no con dos valores fijos.
--    * ENUM nativo: 4 bytes, más que CHAR(1), y obliga a @JdbcTypeCode en
--      Hibernate.
--    * PK compuesta en horarios: ahorra 4 bytes pero obliga a @EmbeddedId.
--
--  NOTA DE ESCALA: todo lo anterior recorta ~20 bytes por fila. Con 100.000
--  usuarios son unos 2 MB. El espacio real lo ocuparán valoraciones.texto y
--  los índices. Estas optimizaciones son correctas y gratuitas, pero lo que
--  decide si la aplicación va rápida son los índices y evitar el N+1.
-- ============================================================================

-- ----------------------------------------------------------------------------
--  Limpieza (solo desarrollo; elimínala si usas Flyway)
-- ----------------------------------------------------------------------------
DROP VIEW  IF EXISTS v_valoraciones_por_pais CASCADE;
DROP VIEW  IF EXISTS v_usuarios_por_pais     CASCADE;
DROP VIEW  IF EXISTS v_locales_mapa          CASCADE;
DROP VIEW  IF EXISTS v_locales_nominatim     CASCADE;
DROP VIEW  IF EXISTS v_locales_puntuacion    CASCADE;
DROP VIEW  IF EXISTS v_consumibles           CASCADE;
DROP VIEW  IF EXISTS v_usuarios              CASCADE;
DROP TABLE IF EXISTS geocodificaciones       CASCADE;
DROP TABLE IF EXISTS valoraciones            CASCADE;
DROP TABLE IF EXISTS consumible_alergenos    CASCADE;
DROP TABLE IF EXISTS alergenos               CASCADE;
DROP TABLE IF EXISTS consumibles             CASCADE;
DROP TABLE IF EXISTS horarios                CASCADE;
DROP TABLE IF EXISTS locales                 CASCADE;
DROP TABLE IF EXISTS usuarios                CASCADE;
DROP TABLE IF EXISTS localidades             CASCADE;
DROP TABLE IF EXISTS paises                  CASCADE;


-- ============================================================================
--  PAISES  (catálogo, clave primaria natural)
--
--  nombre_es se usa como parámetro "country" de la consulta estructurada.
--  codigo se usa como "countrycodes" para acotar la búsqueda (en minúscula).
-- ============================================================================
CREATE TABLE paises (
    codigo    CHAR(2)     NOT NULL,          -- ISO 3166-1 alfa-2, igual que country_code de Nominatim
    nombre_gl VARCHAR(80) NOT NULL,
    nombre_es VARCHAR(80) NOT NULL,
    nombre_en VARCHAR(80) NOT NULL,

    CONSTRAINT pk_paises        PRIMARY KEY (codigo),
    CONSTRAINT ck_paises_codigo CHECK (codigo ~ '^[A-Z]{2}$')
);


-- ============================================================================
--  LOCALIDADES  (catálogo)
--
--  Correspondencia con los campos de Nominatim:
--      nombre    -> city
--      provincia -> county
--      comunidad -> state
--
--  Para España: provincia = "A Coruña", comunidad = "Galicia".
--  Fuera de España, provincia suele quedar NULL y comunidad recoge el nivel
--  administrativo equivalente (Bayern, por ejemplo).
--
--  El bounding box permite encuadrar el mapa en la ciudad sin calcularlo.
-- ============================================================================
CREATE TABLE localidades (
    osm_id       BIGINT,                      -- 8 bytes: los ids de OSM exceden INTEGER
    latitud      DOUBLE PRECISION,            -- 8
    longitud     DOUBLE PRECISION,            -- 8
    bbox_lat_min DOUBLE PRECISION,            -- 8
    bbox_lat_max DOUBLE PRECISION,            -- 8
    bbox_lon_min DOUBLE PRECISION,            -- 8
    bbox_lon_max DOUBLE PRECISION,            -- 8
    id           INTEGER GENERATED BY DEFAULT AS IDENTITY,   -- 4
    osm_type     CHAR(1),                     -- 'N', 'W' o 'R'
    pais_codigo  CHAR(2)     NOT NULL,
    nombre       VARCHAR(80) NOT NULL,        -- Nominatim: city
    provincia    VARCHAR(80),                 -- Nominatim: county
    comunidad    VARCHAR(80),                 -- Nominatim: state

    CONSTRAINT pk_localidades          PRIMARY KEY (id),
    CONSTRAINT fk_localidades_pais     FOREIGN KEY (pais_codigo) REFERENCES paises (codigo),
    CONSTRAINT uq_localidades          UNIQUE NULLS NOT DISTINCT (nombre, provincia, pais_codigo),
    CONSTRAINT uq_localidades_osm      UNIQUE NULLS NOT DISTINCT (osm_type, osm_id),
    CONSTRAINT ck_localidades_osm_type CHECK (osm_type IN ('N','W','R')),
    CONSTRAINT ck_localidades_osm_par  CHECK ((osm_type IS NULL) = (osm_id IS NULL)),
    CONSTRAINT ck_localidades_lat      CHECK (latitud  BETWEEN  -90 AND  90),
    CONSTRAINT ck_localidades_lon      CHECK (longitud BETWEEN -180 AND 180)
);

CREATE INDEX idx_localidades_pais ON localidades (pais_codigo);

COMMENT ON COLUMN localidades.provincia IS 'Parámetro "county" de la consulta estructurada de Nominatim';
COMMENT ON COLUMN localidades.comunidad IS 'Parámetro "state" de la consulta estructurada de Nominatim';


-- ============================================================================
--  USUARIOS
--
--  Atributos compuestos desarrollados:
--    "nombre real"          -> nombre + apellido1 + apellido2
--    "lugar de residencia"  -> FK a localidades
--    "canción del perfil"   -> cancion_titulo + cancion_artista + cancion_url
--
--  Sobre la edad: guardar un entero queda obsoleto cada cumpleaños. Guardamos
--  fecha_nacimiento y exponemos la edad en v_usuarios. Para filtrar por rango
--  de edad SIN perder idx_usuarios_nacimiento:
--      WHERE fecha_nacimiento <= CURRENT_DATE - INTERVAL '18 years'
--        AND fecha_nacimiento >  CURRENT_DATE - INTERVAL '26 years'
--  Nunca EXTRACT(YEAR FROM age(...)) en el WHERE: envolver la columna en una
--  función impide usar el índice (consulta no "sargable").
--
--  Mapeo de sexo en JPA: CHAR(1) necesita un AttributeConverter.
--      @Converter(autoApply = true)
--      public class SexoConverter implements AttributeConverter<Sexo, String> {
--          public String convertToDatabaseColumn(Sexo s) { return s == null ? null : s.getCodigo(); }
--          public Sexo convertToEntityAttribute(String c) { return Sexo.desdeCodigo(c); }
--      }
-- ============================================================================
CREATE TABLE usuarios (
    creado_en        TIMESTAMPTZ NOT NULL DEFAULT now(),           -- 8
    id               INTEGER     GENERATED BY DEFAULT AS IDENTITY, -- 4
    fecha_nacimiento DATE        NOT NULL,                         -- 4
    residencia_id    INTEGER,                                      -- 4, opcional (*)
    puntos           INTEGER     NOT NULL DEFAULT 0,               -- 4
    sexo             CHAR(1),                                      -- opcional (*), 'H' o 'M'
    nombre_usuario   VARCHAR(30)  NOT NULL,
    nombre           VARCHAR(60)  NOT NULL,
    apellido1        VARCHAR(60)  NOT NULL,
    apellido2        VARCHAR(60),                                  -- opcional (*)
    cancion_titulo   VARCHAR(120) NOT NULL,
    cancion_artista  VARCHAR(120) NOT NULL,
    cancion_url      TEXT,                                         -- opcional (*)
    foto_perfil      TEXT,                                         -- opcional (*)

    CONSTRAINT pk_usuarios                PRIMARY KEY (id),
    CONSTRAINT fk_usuarios_residencia     FOREIGN KEY (residencia_id) REFERENCES localidades (id) ON DELETE SET NULL,
    CONSTRAINT uq_usuarios_nombre_usuario UNIQUE (nombre_usuario),
    CONSTRAINT ck_usuarios_nombre_usuario CHECK (nombre_usuario ~ '^[a-z0-9_]{3,30}$'),
    CONSTRAINT ck_usuarios_nacimiento     CHECK (fecha_nacimiento > DATE '1900-01-01'),
    CONSTRAINT ck_usuarios_puntos         CHECK (puntos >= 0),
    CONSTRAINT ck_usuarios_sexo           CHECK (sexo IN ('H','M'))
);

CREATE INDEX idx_usuarios_nacimiento ON usuarios (fecha_nacimiento);
CREATE INDEX idx_usuarios_residencia ON usuarios (residencia_id);
CREATE INDEX idx_usuarios_puntos     ON usuarios (puntos DESC);

COMMENT ON COLUMN usuarios.sexo   IS 'H = hombre, M = mujer. NULL si no lo declara';
COMMENT ON COLUMN usuarios.puntos IS 'Puntos de gamificación acumulados por valorar locales';


-- ============================================================================
--  LOCALES
--
--  Atributo compuesto "ubicación", en formato compatible con Nominatim:
--     via + numero + codigo_postal + FK a localidades + coordenadas OSM
--
--  IMPORTANTE: "via" debe escribirse EXACTAMENTE como aparece en el tag
--  name= de la calle en OpenStreetMap, incluido el idioma. En Santiago las
--  calles están en gallego: "Rúa do Franco", no "Calle del Franco".
--  Comprobadlo en https://www.openstreetmap.org antes de dar una dirección
--  por buena.
--
--  Columnas rellenadas por la geocodificación (ver geocodificar.py):
--     osm_type, osm_id   -> referencia estable del objeto en OSM
--     latitud, longitud  -> coordenadas devueltas por Nominatim
--     bbox_*             -> encuadre del mapa
--     display_name       -> dirección formateada por Nominatim, para mostrar
--     geocodificado_en   -> cuándo se resolvió; NULL = pendiente
--
--  El horario NO va aquí: es multivaluado. Va en la tabla horarios.
-- ============================================================================
CREATE TABLE locales (
    creado_en        TIMESTAMPTZ NOT NULL DEFAULT now(),           -- 8
    geocodificado_en TIMESTAMPTZ,                                  -- 8, NULL = pendiente
    osm_id           BIGINT,                                       -- 8
    latitud          DOUBLE PRECISION,                             -- 8
    longitud         DOUBLE PRECISION,                             -- 8
    bbox_lat_min     DOUBLE PRECISION,                             -- 8
    bbox_lat_max     DOUBLE PRECISION,                             -- 8
    bbox_lon_min     DOUBLE PRECISION,                             -- 8
    bbox_lon_max     DOUBLE PRECISION,                             -- 8
    id               INTEGER GENERATED BY DEFAULT AS IDENTITY,     -- 4
    localidad_id     INTEGER NOT NULL,                             -- 4
    osm_type         CHAR(1),                                      -- 'N', 'W' o 'R'
    codigo_postal    CHAR(5),                                      -- Nominatim: postalcode
    numero           VARCHAR(10),               -- admite "13-18", "s/n", "42 bis"
    nombre           VARCHAR(120) NOT NULL,     -- Nominatim: amenity (nombre del POI)
    via              VARCHAR(120) NOT NULL,     -- tag name= de la calle en OSM
    display_name     TEXT,                      -- dirección formateada que devuelve Nominatim
    foto_perfil      TEXT         NOT NULL,

    CONSTRAINT pk_locales           PRIMARY KEY (id),
    CONSTRAINT fk_locales_localidad FOREIGN KEY (localidad_id) REFERENCES localidades (id),
    CONSTRAINT uq_locales_direccion UNIQUE (nombre, via, numero),
    CONSTRAINT uq_locales_osm       UNIQUE NULLS NOT DISTINCT (osm_type, osm_id),
    CONSTRAINT ck_locales_cp        CHECK (codigo_postal ~ '^[0-9]{5}$'),
    CONSTRAINT ck_locales_osm_type  CHECK (osm_type IN ('N','W','R')),
    CONSTRAINT ck_locales_osm_par   CHECK ((osm_type IS NULL) = (osm_id IS NULL)),
    CONSTRAINT ck_locales_latitud   CHECK (latitud  BETWEEN  -90 AND  90),
    CONSTRAINT ck_locales_longitud  CHECK (longitud BETWEEN -180 AND 180),
    CONSTRAINT ck_locales_bbox      CHECK (bbox_lat_min <= bbox_lat_max AND bbox_lon_min <= bbox_lon_max)
);

CREATE INDEX idx_locales_localidad ON locales (localidad_id);

-- Índice parcial: solo indexa los locales pendientes de geocodificar.
-- Ocupa casi nada y hace instantánea la consulta que alimenta el script.
CREATE INDEX idx_locales_sin_geocodificar ON locales (id) WHERE geocodificado_en IS NULL;

-- Filtrado por recuadro del mapa ("locales visibles en la vista actual").
CREATE INDEX idx_locales_coordenadas ON locales (latitud, longitud);

COMMENT ON COLUMN locales.osm_type     IS 'Tipo de objeto OSM: N = node, W = way, R = relation';
COMMENT ON COLUMN locales.osm_id       IS 'Identificador OSM. BIGINT obligatorio: los ids de nodo superan los 2.147 millones';
COMMENT ON COLUMN locales.display_name IS 'Dirección formateada devuelta por Nominatim. Cachearla evita repetir la consulta';


-- ============================================================================
--  GEOCODIFICACIONES  (caché obligatoria por política de uso)
--
--  Guarda la respuesta cruda de Nominatim para cualquier consulta, incluidas
--  las búsquedas que escriban los usuarios. Antes de llamar a la API hay que
--  mirar aquí: la política prohíbe repetir consultas idénticas.
--
--  consulta_hash es SHA-256 del texto normalizado de la consulta: 32 bytes
--  fijos, y evita indexar una cadena larga de longitud arbitraria.
-- ============================================================================
CREATE TABLE geocodificaciones (
    consultado_en TIMESTAMPTZ NOT NULL DEFAULT now(),   -- 8
    expira_en     TIMESTAMPTZ,                          -- 8, NULL = sin caducidad
    aciertos      INTEGER     NOT NULL DEFAULT 0,       -- 4, cuántas veces se sirvió desde caché
    consulta_hash BYTEA       NOT NULL,                 -- SHA-256 del texto normalizado
    consulta      TEXT        NOT NULL,                 -- consulta original, para depurar
    respuesta     JSONB       NOT NULL,                 -- respuesta completa de Nominatim

    CONSTRAINT pk_geocodificaciones PRIMARY KEY (consulta_hash),
    CONSTRAINT ck_geocodificaciones_hash CHECK (length(consulta_hash) = 32)
);

CREATE INDEX idx_geocodificaciones_expira ON geocodificaciones (expira_en) WHERE expira_en IS NOT NULL;

COMMENT ON TABLE geocodificaciones IS
  'Caché de respuestas de Nominatim. Requisito de https://operations.osmfoundation.org/policies/nominatim/';


-- ============================================================================
--  HORARIOS  (atributo multivaluado de locales)
--
--  Un día sin filas significa "cerrado".
--  Si hora_cierre < hora_apertura, el cierre es de madrugada (12:00-02:00).
-- ============================================================================
CREATE TABLE horarios (
    hora_apertura TIME     NOT NULL,                          -- 8
    hora_cierre   TIME     NOT NULL,                          -- 8
    id            INTEGER  GENERATED BY DEFAULT AS IDENTITY,  -- 4
    local_id      INTEGER  NOT NULL,                          -- 4
    dia_semana    SMALLINT NOT NULL,                          -- 2

    CONSTRAINT pk_horarios       PRIMARY KEY (id),
    CONSTRAINT fk_horarios_local FOREIGN KEY (local_id) REFERENCES locales (id) ON DELETE CASCADE,
    CONSTRAINT uq_horarios_tramo UNIQUE (local_id, dia_semana, hora_apertura),
    CONSTRAINT ck_horarios_dia   CHECK (dia_semana BETWEEN 1 AND 7)
);

CREATE INDEX idx_horarios_local ON horarios (local_id);

COMMENT ON COLUMN horarios.dia_semana IS 'ISO-8601: 1 = lunes ... 7 = domingo';


-- ============================================================================
--  CONSUMIBLES
--  precio = 0.00 representa la tapa incluida con la consumición.
--  tipo: 'T' = tapa, 'B' = bebida. La vista v_consumibles lo expande.
-- ============================================================================
CREATE TABLE consumibles (
    id          INTEGER      GENERATED BY DEFAULT AS IDENTITY,  -- 4
    local_id    INTEGER      NOT NULL,                          -- 4
    tipo        CHAR(1)      NOT NULL,
    precio      NUMERIC(6,2) NOT NULL,
    nombre      VARCHAR(120) NOT NULL,
    descripcion TEXT,
    imagen      TEXT,

    CONSTRAINT pk_consumibles          PRIMARY KEY (id),
    CONSTRAINT fk_consumibles_local    FOREIGN KEY (local_id) REFERENCES locales (id) ON DELETE CASCADE,
    CONSTRAINT uq_consumibles_nombre   UNIQUE (local_id, nombre),
    CONSTRAINT uq_consumibles_id_local UNIQUE (id, local_id),   -- clave candidata para la FK compuesta
    CONSTRAINT ck_consumibles_precio   CHECK (precio >= 0),
    CONSTRAINT ck_consumibles_tipo     CHECK (tipo IN ('T','B'))
);

CREATE INDEX idx_consumibles_local ON consumibles (local_id);
CREATE INDEX idx_consumibles_tipo  ON consumibles (tipo);

COMMENT ON COLUMN consumibles.tipo   IS 'T = tapa, B = bebida';
COMMENT ON COLUMN consumibles.precio IS 'Euros. 0.00 = tapa incluida con la consumición';


-- ============================================================================
--  ALERGENOS  (atributo multivaluado de consumibles)
--
--  Los 14 de declaración obligatoria del Reglamento (UE) 1169/2011.
--  Aquí la tabla SÍ hace falta: un consumible tiene varios A LA VEZ (N:M real),
--  a diferencia del sexo, que es un único valor de una lista de dos.
-- ============================================================================
CREATE TABLE alergenos (
    id     SMALLINT    GENERATED BY DEFAULT AS IDENTITY,   -- 2
    codigo VARCHAR(30) NOT NULL,
    nombre VARCHAR(60) NOT NULL,

    CONSTRAINT pk_alergenos        PRIMARY KEY (id),
    CONSTRAINT uq_alergenos_codigo UNIQUE (codigo)
);

CREATE TABLE consumible_alergenos (
    consumible_id INTEGER  NOT NULL,     -- 4
    alergeno_id   SMALLINT NOT NULL,     -- 2

    CONSTRAINT pk_consumible_alergenos PRIMARY KEY (consumible_id, alergeno_id),
    CONSTRAINT fk_ca_consumible FOREIGN KEY (consumible_id) REFERENCES consumibles (id) ON DELETE CASCADE,
    CONSTRAINT fk_ca_alergeno   FOREIGN KEY (alergeno_id)   REFERENCES alergenos   (id) ON DELETE RESTRICT
);

CREATE INDEX idx_ca_alergeno ON consumible_alergenos (alergeno_id);


-- ============================================================================
--  VALORACIONES
--
--  Relación N:M entre usuarios y locales CON atributos propios: por eso es
--  entidad y no un simple @ManyToMany.
--
--  consumible_id opcional:
--      NULL    -> valoración del local en su conjunto
--      != NULL -> valoración de una tapa o bebida concreta
--
--  La FK compuesta (consumible_id, local_id) impide valorar una tapa
--  atribuyéndola a un local que no la sirve. Con consumible_id NULL la
--  comprobación no se aplica (MATCH SIMPLE).
-- ============================================================================
CREATE TABLE valoraciones (
    fecha         TIMESTAMPTZ NOT NULL DEFAULT now(),           -- 8
    id            INTEGER     GENERATED BY DEFAULT AS IDENTITY, -- 4
    usuario_id    INTEGER     NOT NULL,                         -- 4
    local_id      INTEGER     NOT NULL,                         -- 4
    consumible_id INTEGER,                                      -- 4, opcional
    puntuacion    SMALLINT    NOT NULL,                         -- 2
    texto         TEXT,                                         -- opcional (*)
    foto          TEXT,                                         -- opcional (*)

    CONSTRAINT pk_valoraciones            PRIMARY KEY (id),
    CONSTRAINT fk_valoraciones_usuario    FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE,
    CONSTRAINT fk_valoraciones_local      FOREIGN KEY (local_id)   REFERENCES locales  (id) ON DELETE CASCADE,
    CONSTRAINT fk_valoraciones_consumible FOREIGN KEY (consumible_id, local_id)
                                          REFERENCES consumibles (id, local_id) ON DELETE CASCADE,
    CONSTRAINT uq_valoraciones_unica      UNIQUE NULLS NOT DISTINCT (usuario_id, local_id, consumible_id),
    CONSTRAINT ck_valoraciones_puntuacion CHECK (puntuacion BETWEEN 1 AND 5),
    CONSTRAINT ck_valoraciones_texto      CHECK (texto IS NULL OR length(trim(texto)) > 0)
);

CREATE INDEX idx_valoraciones_local   ON valoraciones (local_id);
CREATE INDEX idx_valoraciones_usuario ON valoraciones (usuario_id);
CREATE INDEX idx_valoraciones_fecha   ON valoraciones (fecha DESC);


-- ============================================================================
--  VISTAS
-- ============================================================================

-- Parámetros listos para la consulta estructurada de Nominatim.
-- El house number solo se envía si es numérico: "s/n" o "13-18" no son
-- números de portal válidos y harían fallar la búsqueda; en esos casos se
-- geocodifica a nivel de calle.
CREATE OR REPLACE VIEW v_locales_nominatim AS
SELECT l.id                                AS local_id,
       l.nombre                            AS amenity,
       CASE WHEN l.numero ~ '^[0-9]+'
            THEN l.numero || ' ' || l.via
            ELSE l.via
       END                                 AS street,
       loc.nombre                          AS city,
       loc.provincia                       AS county,
       loc.comunidad                       AS state,
       p.nombre_es                         AS country,
       lower(p.codigo)                     AS countrycodes,
       l.codigo_postal                     AS postalcode,
       l.geocodificado_en
FROM locales l
JOIN localidades loc ON loc.id = l.localidad_id
JOIN paises      p   ON p.codigo = loc.pais_codigo;

-- Lo que necesita el frontend para pintar el mapa.
-- ATRIBUCIÓN OBLIGATORIA en la interfaz: "© OpenStreetMap contributors".
CREATE OR REPLACE VIEW v_locales_mapa AS
SELECT l.id,
       l.nombre,
       l.via,
       l.numero,
       l.codigo_postal,
       loc.nombre AS localidad,
       l.latitud,
       l.longitud,
       l.bbox_lat_min, l.bbox_lat_max, l.bbox_lon_min, l.bbox_lon_max,
       l.display_name,
       CASE l.osm_type WHEN 'N' THEN 'node' WHEN 'W' THEN 'way' WHEN 'R' THEN 'relation' END AS osm_type,
       l.osm_id,
       CASE WHEN l.osm_type IS NOT NULL
            THEN 'https://www.openstreetmap.org/'
                 || CASE l.osm_type WHEN 'N' THEN 'node' WHEN 'W' THEN 'way' ELSE 'relation' END
                 || '/' || l.osm_id
       END AS osm_url,
       l.foto_perfil,
       COUNT(v.id)                 AS num_valoraciones,
       ROUND(AVG(v.puntuacion), 2) AS puntuacion_media
FROM locales l
JOIN localidades loc     ON loc.id = l.localidad_id
LEFT JOIN valoraciones v ON v.local_id = l.id
WHERE l.latitud IS NOT NULL AND l.longitud IS NOT NULL
GROUP BY l.id, loc.nombre;

CREATE OR REPLACE VIEW v_usuarios AS
SELECT u.id,
       u.nombre_usuario,
       u.nombre,
       u.apellido1,
       u.apellido2,
       u.fecha_nacimiento,
       EXTRACT(YEAR FROM age(u.fecha_nacimiento))::INT AS edad,
       CASE u.sexo WHEN 'H' THEN 'HOMBRE' WHEN 'M' THEN 'MUJER' END AS sexo,
       u.puntos,
       u.foto_perfil,
       u.cancion_titulo,
       u.cancion_artista,
       loc.nombre    AS residencia_localidad,
       loc.provincia AS residencia_provincia,
       p.codigo      AS residencia_pais_codigo,
       p.nombre_es   AS residencia_pais
FROM usuarios u
LEFT JOIN localidades loc ON loc.id = u.residencia_id
LEFT JOIN paises      p   ON p.codigo = loc.pais_codigo;

CREATE OR REPLACE VIEW v_consumibles AS
SELECT c.id,
       c.local_id,
       l.nombre AS local,
       c.nombre,
       c.descripcion,
       c.imagen,
       c.precio,
       CASE c.tipo WHEN 'T' THEN 'TAPA' WHEN 'B' THEN 'BEBIDA' END AS tipo,
       (c.precio = 0) AS tapa_incluida
FROM consumibles c
JOIN locales l ON l.id = c.local_id;

CREATE OR REPLACE VIEW v_locales_puntuacion AS
SELECT l.id AS local_id,
       l.nombre,
       COUNT(v.id)                 AS num_valoraciones,
       ROUND(AVG(v.puntuacion), 2) AS puntuacion_media
FROM locales l
LEFT JOIN valoraciones v ON v.local_id = l.id
GROUP BY l.id, l.nombre;

CREATE OR REPLACE VIEW v_usuarios_por_pais AS
SELECT p.codigo,
       p.nombre_es AS pais,
       COUNT(u.id) AS num_usuarios,
       ROUND(AVG(EXTRACT(YEAR FROM age(u.fecha_nacimiento))), 1) AS edad_media,
       ROUND(AVG(u.puntos), 0) AS puntos_medios
FROM paises p
JOIN localidades loc ON loc.pais_codigo = p.codigo
JOIN usuarios    u   ON u.residencia_id = loc.id
GROUP BY p.codigo, p.nombre_es;

CREATE OR REPLACE VIEW v_valoraciones_por_pais AS
SELECT p.codigo,
       p.nombre_es                 AS pais,
       COUNT(v.id)                 AS num_valoraciones,
       ROUND(AVG(v.puntuacion), 2) AS puntuacion_media
FROM valoraciones v
JOIN usuarios    u   ON u.id = v.usuario_id
JOIN localidades loc ON loc.id = u.residencia_id
JOIN paises      p   ON p.codigo = loc.pais_codigo
GROUP BY p.codigo, p.nombre_es;


-- ============================================================================
--  DISTANCIA ENTRE DOS PUNTOS (fórmula del haversine, en metros)
--
--  Suficiente para "locales a menos de 500 m de mí". Si el proyecto crece y
--  necesitáis consultas espaciales de verdad (polígonos, índices GiST),
--  instalad PostGIS y usad geography(Point, 4326) en lugar de lat/lon sueltas.
-- ============================================================================
CREATE OR REPLACE FUNCTION fn_distancia_m(
    lat1 DOUBLE PRECISION, lon1 DOUBLE PRECISION,
    lat2 DOUBLE PRECISION, lon2 DOUBLE PRECISION
) RETURNS DOUBLE PRECISION AS $$
    SELECT 6371000 * 2 * asin(sqrt(
        power(sin(radians(lat2 - lat1) / 2), 2) +
        cos(radians(lat1)) * cos(radians(lat2)) *
        power(sin(radians(lon2 - lon1) / 2), 2)
    ));
$$ LANGUAGE sql IMMUTABLE STRICT PARALLEL SAFE;


-- ============================================================================
--  GAMIFICACIÓN: +10 puntos por valoración, +5 extra si lleva foto
-- ============================================================================
CREATE OR REPLACE FUNCTION fn_actualizar_puntos() RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'INSERT') THEN
        UPDATE usuarios
           SET puntos = puntos + 10 + CASE WHEN NEW.foto IS NOT NULL THEN 5 ELSE 0 END
         WHERE id = NEW.usuario_id;
    ELSIF (TG_OP = 'DELETE') THEN
        UPDATE usuarios
           SET puntos = GREATEST(0, puntos - 10 - CASE WHEN OLD.foto IS NOT NULL THEN 5 ELSE 0 END)
         WHERE id = OLD.usuario_id;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_valoraciones_puntos
AFTER INSERT OR DELETE ON valoraciones
FOR EACH ROW EXECUTE FUNCTION fn_actualizar_puntos();


-- ============================================================================
--  CONSULTAS DE APOYO
-- ============================================================================
-- Locales pendientes de geocodificar (los que alimenta geocodificar.py):
--   SELECT * FROM v_locales_nominatim WHERE geocodificado_en IS NULL;
--
-- Locales a menos de 500 m del Obradoiro, ordenados por distancia:
--   SELECT nombre, ROUND(fn_distancia_m(latitud, longitud, 42.8805, -8.5456)::numeric) AS metros
--     FROM locales
--    WHERE latitud IS NOT NULL
--      AND fn_distancia_m(latitud, longitud, 42.8805, -8.5456) < 500
--    ORDER BY metros;
--
-- Tamaño de cada tabla con sus índices:
--   SELECT relname AS tabla,
--          pg_size_pretty(pg_total_relation_size(relid)) AS total,
--          pg_size_pretty(pg_relation_size(relid))       AS datos,
--          pg_size_pretty(pg_indexes_size(relid))        AS indices
--     FROM pg_catalog.pg_statio_user_tables
--    ORDER BY pg_total_relation_size(relid) DESC;