# NutriVida – Backend (API REST)

API REST de NutriVida: autenticación con JWT, usuarios y roles, catálogo, inventario, carrito, pedidos, ventas y auditoría.
Proyecto de la asignatura DSY1104 Desarrollo Full Stack II (Duoc UC).

- **Stack:** Java 21 · Spring Boot 4.1.2 · Spring Security + JWT · Spring Data JPA · Swagger (springdoc) · H2 / MySQL
- **Frontend:** https://github.com/PatocucaF/nutrivida-frontend

## Cómo correrlo

Requisitos: Java 21 (no hace falta instalar Maven, el proyecto trae `mvnw`).

```powershell
.\mvnw.cmd spring-boot:run      # Windows
./mvnw spring-boot:run          # Linux / Mac
```

La API queda en `http://localhost:8080`. Por defecto usa una base **H2 en memoria** (se borra al apagar).

| Qué | URL |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Estado (heartbeat) | http://localhost:8080/actuator/health |
| Consola H2 (solo H2) | http://localhost:8080/h2-console |

Pruebas automáticas: `.\mvnw.cmd clean test`

## Cuentas de prueba

Se crean solas al iniciar (datos de demo).

| Rol | Correo | Contraseña | Puede |
|---|---|---|---|
| ADMIN | admin@nutrivida.cl | Admin12345 | Todo |
| VENDEDOR | vendedor@nutrivida.cl | Vendedor123 | Ver productos y pedidos (lista y detalle) |
| CLIENTE | cliente@nutrivida.cl | Cliente123 | Comprar: catálogo, carrito y sus pedidos |

El registro público (`/api/v1/auth/register`) siempre crea un CLIENTE.

## Probar en Swagger

1. `POST /api/v1/auth/login` con un correo y contraseña de la tabla. Copiar el valor de `token`.
2. Botón **Authorize** (arriba a la derecha) → pegar solo el token → *Authorize*.
3. Probar cualquier endpoint. En los listados paginados, **borrar el valor `string` del campo `sort`** (Swagger lo trae de ejemplo y no es una columna real).

## Endpoints

Todos van bajo `/api/v1`. Los de `auth` son públicos; el resto exige el header `Authorization: Bearer <token>`.

| Grupo | Método y ruta | Rol |
|---|---|---|
| Auth | `POST /auth/register`, `POST /auth/login`, `POST /auth/refresh` | Público (refresh: token vigente) |
| Perfil | `GET /users/me`, `PUT /users/me`, `PUT /users/me/password` | Cualquier usuario |
| Usuarios | `GET /admin/users`, `GET /admin/users/{id}`, `POST /admin/users`, `PUT /admin/users/{id}`, `DELETE /admin/users/{id}`, `PATCH /admin/users/{id}/status` | ADMIN |
| Auditoría | `GET /admin/audit` | ADMIN |
| Categorías | `GET /categories` (todos) · `POST`, `PUT /{id}`, `DELETE /{id}` | ADMIN para escribir |
| Productos | `GET /products`, `GET /products/{id}` (todos) · `POST`, `PUT /{id}` | ADMIN para escribir |
| Inventario | `POST /inventory/products/{id}/adjustments`, `GET /inventory/products/{id}/movements` | ADMIN |
| Carrito | `GET /cart`, `POST/PUT/DELETE /cart/items/{productId}`, `DELETE /cart` | CLIENTE |
| Pedidos (cliente) | `POST /orders/checkout`, `GET /orders`, `GET /orders/{id}` | CLIENTE |
| Pedidos (admin) | `GET /admin/orders`, `GET /admin/orders/{id}` · `PATCH /admin/orders/{id}/cancel` | ADMIN y VENDEDOR · cancelar solo ADMIN |
| Ventas | `GET /sales` | ADMIN |

Detalle de cada endpoint (parámetros, respuestas y errores) en Swagger.

## Seguridad (JWT)

- **Generación:** `POST /auth/login` entrega un token firmado (HS256) con el correo y los roles.
- **Verificación:** un filtro valida firma, vencimiento y que el usuario siga activo en cada request; si falla responde `401`. Los permisos por rol se aplican con `@PreAuthorize` (`403` si el rol no corresponde).
- **Renovación:** `POST /auth/refresh` con el token vigente entrega uno nuevo con la expiración reiniciada. El frontend lo hace solo antes de que venza.

## Variables de entorno

Las variables de RDS son obligatorias solo al activar el perfil `mysql`; por defecto
la aplicación usa H2.

| Variable | Para qué | Valor por defecto |
|---|---|---|
| `JWT_SECRET` | Clave con que se firman los tokens (**cambiarla en producción**) | clave de desarrollo |
| `JWT_EXPIRATION_MS` | Duración del token | `86400000` (24 h) |
| `ADMIN_EMAIL`, `ADMIN_PASSWORD` | Administrador inicial | `admin@nutrivida.cl`, `Admin12345` |
| `CORS_ORIGINS` | URL(s) del frontend, separadas por coma | `http://localhost:5173` |
| `DEMO_DATA` | `false` desactiva los datos de ejemplo | `true` |
| `SPRING_PROFILES_ACTIVE` | `mysql` para usar AWS RDS MySQL | (H2) |
| `AWS_DB_HOST`, `AWS_DB_PORT`, `AWS_DB_NAME`, `AWS_DB_USER`, `AWS_DB_PASSWORD` | Conexión MySQL de RDS | Host, nombre de BD, usuario y contraseña requeridos; puerto `3306` |
| `AWS_DB_SSL_MODE` | Validación TLS del servidor | `VERIFY_IDENTITY` |

## Usar MySQL

Para probar con AWS RDS, copia `.env.example` a `.env` y completa el endpoint RDS,
el nombre de la base de datos y las credenciales. `.env` se carga al activar el perfil
`mysql` y está excluido de Git. No guardes credenciales reales en `.env.example`.

```powershell
$env:SPRING_PROFILES_ACTIVE = "mysql"
.\mvnw.cmd spring-boot:run
```

Para MySQL local con Docker, configura en `.env` las credenciales de `docker-compose.yml`,
establece `AWS_DB_SSL_MODE=DISABLED` (solo para desarrollo local) y levanta el contenedor:

```powershell
docker compose up -d
$env:SPRING_PROFILES_ACTIVE = "mysql"
.\mvnw.cmd spring-boot:run
```

El perfil valida la conexión al iniciar, crea los roles base si faltan y Hibernate actualiza
las tablas automáticamente. La conexión TLS verifica el certificado y el hostname del servidor
RDS. Diagnóstico de conexión: `GET /api/v1/health/db` (responde `503` si la base no está disponible).

## Estructura

```
src/main/java/com/duoc/nutrivida
├── auth       registro, login y renovación de token
├── user       usuarios, roles y perfil
├── catalog    categorías, productos e inventario
├── commerce   carrito, pedidos y ventas
├── audit      registro de auditoría
├── health     diagnóstico de conexión a la base de datos
├── security   JWT, filtro y reglas de acceso
├── config     Swagger, inicialización y datos de demo
└── common     errores y manejo global de excepciones
```
