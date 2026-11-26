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
./gradlew bootRun                                          # datos de prueba de siempre
./gradlew bootRun --args='--spring.profiles.active=demo'   # con los datos de la demo
```

Spring Boot sirve también las dos webs:

- Clientes: http://localhost:8080/clientes/index.html (`adrianql5` / `password`)
- Propietarios: http://localhost:8080/propietarios/index.html (`hugoocoto` / `password`)
