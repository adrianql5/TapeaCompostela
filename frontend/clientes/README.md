# TapeaCompostela para clientes

La app del móvil para los clientes: un mapa de Compostela con los bares, sus reseñas y sus recompensas, y el QR
para canjearlas. Hecha con HTML, CSS y JavaScript, sin instalar nada: la sirve el propio Spring Boot.

## Abrirla

```bash
cd backend
./gradlew bootRun
```

y en el navegador `http://localhost:8080/clientes/index.html`.

Para verla como en un móvil desde el ordenador: herramientas de desarrollador del navegador (F12) y el botón del
móvil. Para abrirla en un móvil de verdad hace falta HTTPS (la guía de la demo lo explica).

Usuarios de prueba: `adrianql5`, `maria_tapas`, `xoan_compos`... todos con la contraseña `password`.

## Qué hay en cada archivo

| Archivo | Qué hace |
|---|---|
| `index.html` | Lo fijo de la pantalla: el mapa, la barra de búsqueda y los botones. Carga Leaflet y qrcode-generator |
| `styles.css` | Colores, letras y estilos de toda la app |
| `js/app.js` | El estado de la app y `render()`, que pone la pantalla al día; abre las demás pantallas |
| `js/api.js` | Todas las llamadas a la API, los tokens y los textos de error |
| `js/map.js` | El mapa y los pines (Leaflet + OpenStreetMap) |
| `js/business.js` | La ficha de un local: recompensas, reseñas, canjear y escribir reseña |
| `js/qr.js` | El QR que cambia cada 30 segundos |
| `js/redemptions.js` | Tus canjes |
| `js/profile.js` | Tus puntos y cerrar sesión |
| `js/auth.js` | Entrar o crear una cuenta |
| `js/ui.js` | Piezas comunes: escapar texto, iconos, avisos, la hoja que sube desde abajo, estrellas |
| `js/format.js` | Precios, notas, distancias y fechas en texto |
