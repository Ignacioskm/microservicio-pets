# 🐕 VitalPet Connect — ms-pets

Microservicio de **mascotas** (pacientes y catálogo de especies) de VitalPet Connect, aislado del monorepo para correr **por cuenta propia** (Spring Boot 3.2 · Java 17 · MySQL 8).

> Asignatura: Desarrollo Fullstack II · Integrantes: Ignacio Mellado 

## Endpoints

Base: `http://<host>:8083/api/pets`

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/` | Mascotas **activas** |
| GET | `/{id}` | Una mascota (incluso si está inactiva) |
| POST | `/` | Crear mascota → `201` |
| PUT | `/{id}` | Actualizar (se envía el objeto completo) |
| DELETE | `/{id}` | Desactivar (borrado lógico) → `204` |
| GET | `/{ownerId}/owner` | Mascotas de un dueño (**incluye inactivas**) |
| GET | `/available` | Mascotas sin dueño (para adopción) |
| PUT | `/{petId}/owner/{userId}` | Asignar dueño → `204` |
| GET | `/{id}/exists` | `true`/`false` |
| GET | `/species` | Catálogo de especies (`DOG`, `CAT`, `BIRD`, `Other`) |

**Crear/actualizar** (`PetRequestDTO`):

```json
{
  "name": "Rocky",
  "breed": "Labrador",
  "birthDate": "2022-03-15",
  "weight": 28.5,
  "speciesId": 1,
  "ownerId": 3
}
```

`name`, `breed`, `birthDate` y `speciesId` son obligatorios; `ownerId` es opcional (`null` = sin dueño).

**Errores** (todos con la misma forma): `{ "timestamp", "status", "error", "message", "details": [] }` — `400` validación/regla de negocio, `404` no encontrado, `409` conflicto en BD, `503` ms-users no responde (solo si la validación está activa).

Swagger UI: `http://<host>:8083/swagger-ui.html`

## Configuración (variables de entorno)

| Variable | Por defecto | Descripción |
|---|---|---|
| `SERVER_PORT` | `8083` | Puerto HTTP |
| `DB_URL` | `jdbc:mysql://localhost:3306/vitalpet_pets?...` | JDBC de MySQL |
| `DB_USER` / `DB_PASSWORD` | `root` / `root` | Credenciales de MySQL |
| `DDL_AUTO` | `update` | `update`, `validate`, `create` (⚠ `create` borra los datos) |
| `USERS_VALIDATION_ENABLED` | `false` | `true` = valida el dueño contra ms-users |
| `USERS_SERVICE_URL` | `http://localhost:8082` | URL base de ms-users |
| `CORS_ALLOWED_ORIGINS` | `*` | Orígenes permitidos, separados por coma |
| `JAVA_OPTS` (solo Docker) | `-Xmx256m -Xms128m` | Memoria de la JVM |

## Ejecución local

**Con Docker (recomendado):**

```bash
cp .env.example .env        # edita las claves
docker compose up -d --build
curl http://localhost:8083/api/pets/species
```

**Sin Docker** (JDK 17 + Maven 3.9 + una MySQL con la BD `vitalpet_pets` creada):

```bash
DB_USER=root DB_PASSWORD=tu_clave mvn spring-boot:run
```

Tests: `mvn test` (el reporte de cobertura queda en `docs/coverage/`).

### Datos de demo (opcional)

```bash
./scripts/seed-demo.sh http://localhost:8083
```

Crea 7 mascotas equivalentes a las del `mockData` del front (dueños 2, 3 y 4 = usuarios mock). No es idempotente.


## Integración con ms-users (cuando esté disponible)

Con `USERS_VALIDATION_ENABLED=true`, al crear/actualizar una mascota con `ownerId` o al asignar dueño, ms-pets llama a:

- `GET {USERS_SERVICE_URL}/api/user/{id}/exists`
- `GET {USERS_SERVICE_URL}/api/user/{id}/is-client`

Edita el `.env` con la URL de la EC2 de tu compañero (ojo: debe llegar al puerto de ms-users, y su Security Group debe permitir tu IP) y reinicia: `docker compose up -d`.

## Notas

- **Sin autenticación:** igual que en el monorepo, ms-pets no valida JWT (tampoco lo hacía el Gateway). Cualquiera que llegue al puerto 8083 puede leer y modificar mascotas.
- `GET /{ownerId}/owner` y `GET /{id}` devuelven también mascotas desactivadas; el cliente debe filtrar por `active`.
- La especie viaja como `speciesId`; el nombre sale de `GET /species`.
