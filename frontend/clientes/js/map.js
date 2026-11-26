import { rating } from './format.js';

// El mapa con Leaflet (variable global L, se carga en index.html). Leaflet es JavaScript normal: se crea el mapa una
// vez y luego se le añaden, cambian o quitan los pines

// El centro de Compostela (la Praza do Obradoiro), donde abre el mapa si todavía no hay locales
const SANTIAGO = [42.8806, -8.5446];

// Desde este zoom los pines enseñan la nota; más lejos son puntos, porque los bares del casco histórico están a
// pocos metros y las píldoras se taparían unas a otras
const LABEL_ZOOM = 17;

// El pin de un local: un divIcon, un marcador hecho con nuestro propio HTML (las clases están en styles.css)
function pinIcon(business, selected, compact) {
  const isNew = business.ratingCount === 0;
  if (compact && !selected) {
    return L.divIcon({ className: 'pin-icon', html: `<div class="dot ${isNew ? 'is-new' : ''}"></div>`, iconSize: [22, 22], iconAnchor: [11, 11] });
  }
  const label = isNew ? 'Nuevo' : `<span class="star">★</span>${rating(business.averageRating)}`;
  const classes = ['pin', isNew && 'is-new', selected && 'is-selected'].filter(Boolean).join(' ');
  return L.divIcon({ className: 'pin-icon', html: `<div class="${classes}">${label}</div>`, iconSize: [0, 0], iconAnchor: [0, 0] });
}

const ME_ICON = L.divIcon({ className: 'pin-icon', html: '<div class="me-dot"></div>', iconSize: [18, 18], iconAnchor: [9, 9] });

// Crea el mapa en el div #map. onSelect(id) se llama al tocar un pin, y onSelect(null) al tocar el mapa fuera de
// los pines (cierra la ficha del local). Devuelve update(), que pone el mapa al día con lo que hay que enseñar
export function createMap({ onSelect }) {
  const map = L.map('map', { zoomControl: false }).setView(SANTIAGO, 15);
  // El mapa de OpenStreetMap, gratis y sin clave. styles.css le baja el color para que destaquen los pines
  L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>',
    maxZoom: 19,
  }).addTo(map);

  const markers = new Map();   // id del local -> su marcador
  let meMarker = null;
  let fitted = false;
  let lastTarget = null;
  let current = { businesses: [], selectedId: null, me: null, focus: null };

  map.on('click', () => onSelect(null));
  // Al cambiar el zoom se vuelven a pintar los pines: puntos o píldoras
  map.on('zoomend', () => update(current));

  // businesses: solo los que pasan la búsqueda y los filtros; los demás se quitan del mapa
  function update({ businesses, selectedId, me, focus }) {
    current = { businesses, selectedId, me, focus };
    const compact = map.getZoom() < LABEL_ZOOM;

    for (const [id, marker] of markers) {
      if (!businesses.some(b => b.id === id)) {
        marker.remove();
        markers.delete(id);
      }
    }
    for (const business of businesses) {
      let marker = markers.get(business.id);
      if (!marker) {
        marker = L.marker([business.latitude, business.longitude], { title: business.name, alt: business.name, keyboard: true })
          .on('click', () => onSelect(business.id))
          .addTo(map);
        markers.set(business.id, marker);
      }
      marker.setIcon(pinIcon(business, business.id === selectedId, compact));
      marker.setZIndexOffset(business.id === selectedId ? 1000 : 0);
    }

    if (me && !meMarker) meMarker = L.marker([me.latitude, me.longitude], { icon: ME_ICON, interactive: false }).addTo(map);
    else if (me) meMarker.setLatLng([me.latitude, me.longitude]);

    // La primera vez que llegan los locales, el mapa se encuadra para verlos todos (dejando sitio a la barra de arriba)
    if (!fitted && businesses.length > 0) {
      fitted = true;
      map.fitBounds(businesses.map(b => [b.latitude, b.longitude]), { paddingTopLeft: [30, 150], paddingBottomRight: [80, 40], maxZoom: 17 });
    }

    // El mapa vuela al local elegido o, si no hay, a tu ubicación. Solo cuando cambia el destino
    const selected = businesses.find(b => b.id === selectedId);
    const target = selected ? [selected.latitude, selected.longitude] : focus ? [focus.latitude, focus.longitude] : null;
    if (String(target) !== String(lastTarget)) {
      lastTarget = target;
      if (target) flyTo(target);
    }
  }

  // Se acerca hasta que se ven las notas (si ya estabas más cerca, no te aleja) y deja el pin en el tercio de arriba,
  // para que la hoja de abajo no lo tape
  function flyTo(target) {
    const zoom = Math.max(map.getZoom(), LABEL_ZOOM);
    const point = map.project(target, zoom).add([0, map.getSize().y * 0.22]);
    map.flyTo(map.unproject(point, zoom), zoom, { duration: 0.6 });
  }

  return { update };
}
