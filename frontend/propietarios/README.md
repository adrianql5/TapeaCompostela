# TapeaCompostela para locales

La app del móvil para los dueños de los bares: sus locales, sus recompensas, lo que dicen los clientes y el
escáner que valida los canjes. Hecha con HTML, CSS y JavaScript, sin instalar nada: la sirve el propio Spring Boot.

## Abrirla

```bash
cd backend
./gradlew bootRun
```

y en el navegador `http://localhost:8080/propietarios/index.html`.

La cámara del escáner solo funciona en `localhost` o con HTTPS (la guía de la demo explica cómo abrirla en un
móvil). Sin cámara se puede pegar el texto del QR a mano.

Usuario de prueba con locales: `hugoocoto`, contraseña `password`.

## Qué hay en cada archivo

| Archivo | Qué hace |
|---|---|
| `index.html` | El contenedor de la app. Carga Leaflet y jsQR |
| `styles.css` | Colores, letras y estilos de toda la app |
| `js/app.js` | Tus locales y el botón de escanear; abre las demás pantallas |
| `js/api.js` | Todas las llamadas a la API, los tokens y los textos de error |
| `js/auth.js` | Entrar o crear una cuenta de local |
| `js/business-detail.js` | Un local: cifras, recompensas, reseñas, editar y borrar |
| `js/new-business.js` | El alta de un local en 4 pasos |
| `js/business-fields.js` | Los campos de un local, compartidos por el alta y la edición |
| `js/location-picker.js` | Elegir la ubicación moviendo el mapa, y el mapa pequeño de la ficha |
| `js/scanner.js` | La cámara, jsQR y el resultado del canje |
| `js/ui.js` | Piezas comunes: escapar texto, iconos, avisos, la hoja que sube desde abajo, estrellas |
| `js/format.js` | Precios, notas y fechas en texto |
