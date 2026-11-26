// Pequeñas funciones para enseñar los datos de la API en la pantalla

export function priceSymbols(priceRange) {
  return { CHEAP: 1, MODERATE: 2, EXPENSIVE: 3 }[priceRange] ?? 0;
}

export const PRICE_LABEL = { CHEAP: 'Barato', MODERATE: 'Normal', EXPENSIVE: 'Caro' };

export function rating(value) {
  return Number(value).toFixed(1).replace('.', ',');
}

// Distancia en línea recta entre dos puntos (fórmula del haversine), en metros
export function distance(from, to) {
  const rad = deg => (deg * Math.PI) / 180;
  const dLat = rad(to.latitude - from.latitude);
  const dLon = rad(to.longitude - from.longitude);
  const a = Math.sin(dLat / 2) ** 2 + Math.cos(rad(from.latitude)) * Math.cos(rad(to.latitude)) * Math.sin(dLon / 2) ** 2;
  return 6371000 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
}

// "350 m · 4 min" andando (unos 80 metros por minuto)
export function walking(meters) {
  const minutes = Math.max(1, Math.round(meters / 80));
  const text = meters < 1000 ? `${Math.round(meters / 10) * 10} m` : `${(meters / 1000).toFixed(1).replace('.', ',')} km`;
  return `${text}, ${minutes} min andando`;
}

export function shortDate(iso) {
  return new Date(iso).toLocaleDateString('es-ES', { day: 'numeric', month: 'short' });
}
