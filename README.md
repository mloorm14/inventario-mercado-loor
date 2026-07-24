# Inventario Mercado Loor

Sistema de gestión de inventario del Mercado Municipal de Quevedo — examen parcial de
Aplicaciones Web, con Spring Boot, autenticación JWT y cache Redis.

## Datos del estudiante

- **Nombre:** Loor Marlon
- **Universidad:** Universidad Técnica Estatal de Quevedo (UTEQ)
- **Asignatura:** Aplicaciones Web
- **Proyecto:** Sistema de gestión de inventario del Mercado Municipal de Quevedo

## Versiones

- **Java:** 21 LTS
- **Spring Boot:** 3.5.16

## Requisitos previos

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (incluye Docker Compose)

No se necesita instalar Java, Maven ni PostgreSQL/Redis localmente — todo corre dentro
de los contenedores.

## Arranque en un solo comando

```bash
git clone https://github.com/mloorm14/inventario-mercado-loor.git
cd inventario-mercado-loor
cp .env.example .env
docker compose up -d --build
```

Esto levanta tres servicios:

| Servicio | Puerto | Descripción |
|---|---|---|
| `app` | 8080 | API Spring Boot |
| `postgres` | 5432 | Base de datos (con `db/schema.sql` y `db/seed.sql` aplicados automáticamente en el primer arranque) |
| `redis` | 6379 | Cache del listado de productos |

Para confirmar que los tres servicios están arriba:

```bash
docker compose ps
```

## Usuarios semilla y obtención del token JWT

`db/seed.sql` crea dos usuarios de prueba (contraseñas en texto plano documentadas ahí
mismo, hasheadas con BCrypt en la base de datos):

| username | password | rol |
|---|---|---|
| `admin.mercado` | `Admin#Quevedo2026` | `ROLE_ADMIN` |
| `usuario.mercado` | `User#Quevedo2026` | `ROLE_USER` |

El token se obtiene con `POST /api/v1/auth/login`:

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin.mercado","password":"Admin#Quevedo2026"}'
```

Respuesta:

```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9..."
  },
  "message": "Login exitoso"
}
```

El token dura **1 hora** (`jwt.expiration-ms` en `application.yml`). Se envía en cada
petición protegida con el header `Authorization: Bearer <token>`.

## URL base de la API

```
http://localhost:8080/api/v1/productos
```

## Cómo probar los requisitos funcionales

En los ejemplos, `$ADMIN_TOKEN` y `$USER_TOKEN` son los tokens obtenidos vía login con
cada usuario semilla respectivamente.

### 1. Listado paginado (`GET`, requiere `ROLE_USER` o `ROLE_ADMIN`)

```bash
curl "http://localhost:8080/api/v1/productos?page=0&size=20" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

Respuesta `200`, con `meta` poblado:

```json
{
  "success": true,
  "data": [],
  "message": "Listado obtenido correctamente",
  "meta": { "page": 0, "size": 20, "totalElements": 0, "totalPages": 0 }
}
```

**Sin token** → `401`:

```bash
curl -i "http://localhost:8080/api/v1/productos"
```

```json
{ "success": false, "message": "No autenticado. Se requiere un token JWT valido." }
```

### 2. Creación validada (`POST`, requiere `ROLE_ADMIN`)

```bash
curl -X POST http://localhost:8080/api/v1/productos \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H "Content-Type: application/json" \
  -d '{"nombre":"Platano","categoria":"Frutas","stock":10,"precio":0.50}'
```

Respuesta `201` con el producto creado.

**Con token de `ROLE_USER`** (rol insuficiente) → `403`:

```bash
curl -i -X POST http://localhost:8080/api/v1/productos \
  -H "Authorization: Bearer $USER_TOKEN" -H "Content-Type: application/json" \
  -d '{"nombre":"Platano","categoria":"Frutas","stock":10,"precio":0.50}'
```

```json
{ "success": false, "message": "Acceso denegado. No tiene permisos suficientes." }
```

**Body inválido** (`nombre` vacío, `precio` en 0) → `400` con el detalle por campo:

```bash
curl -i -X POST http://localhost:8080/api/v1/productos \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H "Content-Type: application/json" \
  -d '{"nombre":"","categoria":"Frutas","stock":10,"precio":0}'
```

```json
{
  "success": false,
  "message": "Error de validacion",
  "errors": [
    { "field": "nombre", "message": "must not be blank" },
    { "field": "precio", "message": "must be greater than or equal to 0.01" }
  ]
}
```

### 3. Eliminación lógica (`DELETE`, requiere `ROLE_ADMIN`)

```bash
curl -X DELETE http://localhost:8080/api/v1/productos/1 \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

Respuesta `200`. El producto no se borra físicamente: se marca `activo = false` y deja
de aparecer en el listado (`findByActivoTrue`).

**Id inexistente** → `404`:

```bash
curl -i -X DELETE http://localhost:8080/api/v1/productos/9999 \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

```json
{ "success": false, "message": "Producto con id 9999 no encontrado" }
```

### 4. Cache Redis (patrón cache-aside)

El listado (`GET /api/v1/productos`) cachea por combinación de `page`/`size`/`sort`
durante 10 minutos. `POST` y `DELETE` invalidan toda la caché de productos
(`@CacheEvict(allEntries = true)`).

Para comprobarlo:

```bash
# 1) Primer GET: cache miss, puebla Redis
curl -s "http://localhost:8080/api/v1/productos?page=0&size=20" -H "Authorization: Bearer $ADMIN_TOKEN" > /dev/null

# 2) Confirmar que la clave existe en Redis
docker exec inventario-mercado-loor-redis-1 redis-cli KEYS "productos*"
# -> productos::page:0:size:20:sort:UNSORTED

# 3) Segundo GET identico: cache hit (mismo resultado, sin volver a consultar Postgres)
curl -s "http://localhost:8080/api/v1/productos?page=0&size=20" -H "Authorization: Bearer $ADMIN_TOKEN"
```

### 5. JWT con roles (`GET` = `ROLE_USER`/`ROLE_ADMIN`, `POST`/`DELETE` = solo `ROLE_ADMIN`)

Resumen de las reglas de autorización (`SecurityConfig`):

| Endpoint | Rol requerido | Sin token | Rol insuficiente |
|---|---|---|---|
| `GET /api/v1/productos` | `ROLE_USER` o `ROLE_ADMIN` | `401` | — |
| `POST /api/v1/productos` | `ROLE_ADMIN` | `401` | `403` |
| `DELETE /api/v1/productos/{id}` | `ROLE_ADMIN` | `401` | `403` |

## Informe técnico (LaTeX)

El informe técnico completo (arquitectura, contrato de API, modelo de datos, decisiones
de seguridad y cache) está en `docs/informe/informe.tex`. Para compilarlo a PDF:

```bash
cd docs/informe && pdflatex informe && bibtex informe && pdflatex informe && pdflatex informe
```

Genera `docs/informe/informe.pdf`.
