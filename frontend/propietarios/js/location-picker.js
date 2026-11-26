import { el, icon, toast } from './ui.js';

// El centro de Compostela (la Praza do Obradoiro)
export const SANTIAGO = { latitude: 42.8806, longitude: -8.5446 };

const TILES = 'https://tile.openstreetmap.org/{z}/{x}/{y}.png';
const ATTRIBUTION = '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>';

// Elegir dónde está el local moviendo el mapa: el pin se queda quieto en el centro, como al pedir un taxi.
// Así nadie tiene que saber qué es una latitud. onChange recibe { latitude, longitude } cada vez que el mapa para
export function locationPicker(value, onChange) {
  const box = el(`
    <div class="picker">
      <div class="picker-map"></div>
      <div class="crosshair-dot"></div>
      <div class="crosshair">${icon('pin', 44)}</div>
      <button type="button" class="icon-btn picker-locate" aria-label="Ir a mi ubicación">${icon('locate')}</button>
    </div>`);

  // Leaflet necesita que el div ya esté en la página para saber cuánto mide: el mapa se crea justo después
  setTimeout(() => {
    const map = L.map(box.querySelector('.picker-map'), { zoomControl: false }).setView([value.latitude, value.longitude], 17);
    L.tileLayer(TILES, { attribution: ATTRIBUTION, maxZoom: 19 }).addTo(map);
    map.on('moveend', () => {
      const center = map.getCenter();
      // 6 decimales son unos 10 cm: más no sirve de nada
      onChange({ latitude: Number(center.lat.toFixed(6)), longitude: Number(center.lng.toFixed(6)) });
    });

    box.querySelector('.picker-locate').addEventListener('click', () => {
      navigator.geolocation?.getCurrentPosition(
        position => map.setView([position.coords.latitude, position.coords.longitude], 18),
        () => toast('Sin permiso para usar tu ubicación. Mueve el mapa con el dedo', 'error'),
        { enableHighAccuracy: true, timeout: 10000 });
    });
  });

  return box;
}

const DOT = L.divIcon({ className: 'pin-icon', html: '<div class="dot"></div>', iconSize: [22, 22], iconAnchor: [11, 11] });

// Un mapa pequeño que solo enseña dónde está el local (no se puede mover)
export function miniMap(latitude, longitude) {
  const box = el('<div class="mini-map"><div style="height: 100%"></div></div>');
  setTimeout(() => {
    const map = L.map(box.firstElementChild, {
      zoomControl: false, dragging: false, scrollWheelZoom: false, doubleClickZoom: false, touchZoom: false,
      keyboard: false, attributionControl: false,
    }).setView([latitude, longitude], 17);
    L.tileLayer(TILES).addTo(map);
    L.marker([latitude, longitude], { icon: DOT, interactive: false }).addTo(map);
  });
  return box;
}
