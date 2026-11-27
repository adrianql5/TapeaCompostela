# TapeaCompostela

| Carpeta | Qué hay |
|---|---|
| `backend/` | La API REST con Spring Boot (Gradle) |
| `frontend/clientes/` | La web de los clientes: mapa, reseñas, recompensas y el QR para canjear |
| `frontend/propietarios/` | La web de los dueños: sus locales, sus recompensas y el escáner de QR |
| `compose.yaml` | La base de datos PostgreSQL |
| `compose-keycloak.yaml`, `keycloak/` | Keycloak, para el perfil `oidc` |

## Arrancar

```bash
cd backend
./gradlew bootRun          # arranca también PostgreSQL (Docker) y carga los datos de prueba
```

Spring Boot sirve también las dos webs:

- Clientes: http://localhost:8080/clientes/index.html (`adrianql5` / `password`)
- Propietarios: http://localhost:8080/propietarios/index.html (`hugoocoto` / `password`)

## Fallos conocidos y mejoras

- **Se pueden conseguir puntos sin límite con las reseñas.** Cada reseña da 10 puntos (`ReviewCreatedEvent` → `PointsEventListener`), pero al borrarla no se quitan. Un usuario puede escribir una reseña, borrarla y volver a escribirla tantas veces como quiera. Mejora: al borrar la reseña, publicar un evento que reste esos 10 puntos (o no dejar escribir otra reseña en el mismo local).
- **Los puntos sirven en cualquier local.** Los puntos son un único saldo del usuario (`users.points`), así que los conseguidos con las visitas o reseñas de un dueño se pueden gastar en las recompensas de un local de otro dueño. Ese dueño acaba pagando recompensas que no ha generado su local. Mejora: guardar los puntos por local (o por dueño) y gastar en cada recompensa solo los del mismo dueño.
