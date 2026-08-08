# API de Empleados

Base URL local: `http://localhost:8080`

## Listar empleados

```
GET /api/v1/employees
```

**Request:** sin parámetros ni body.

**Response:** `200 OK`

```json
[
  {
    "id": 1,
    "fullName": "Ana Gómez",
    "email": "ana.gomez@example.com",
    "documentType": "CC",
    "documentNumber": "1020304050",
    "active": true
  }
]
```

Si no hay empleados registrados, responde `200 OK` con un arreglo vacío `[]`.

## Consultar empleado por id

```
GET /api/v1/employees/{id}
```

**Request:** sin body. `{id}` es el id numérico del empleado.

**Response:** `200 OK`

```json
{
  "id": 1,
  "fullName": "Ana Gómez",
  "email": "ana.gomez@example.com",
  "documentType": "CC",
  "documentNumber": "1020304050",
  "active": true
}
```

**`404 Not Found`** — no existe un empleado con ese id:

```json
{
  "timestamp": "2026-08-08T10:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "No existe el empleado 99"
}
```

## Crear empleado

```
POST /api/v1/employees
Content-Type: application/json
```

**Request body:**

```json
{
  "fullName": "Ana Gómez",
  "email": "ana.gomez@example.com",
  "documentType": "CC",
  "documentNumber": "1020304050"
}
```

| Campo            | Tipo   | Obligatorio | Notas                                      |
|-------------------|--------|:-----------:|---------------------------------------------|
| `fullName`        | string | sí           | no puede estar vacío                        |
| `email`           | string | sí           | debe ser un email válido y único            |
| `documentType`    | string | sí           | uno de: `CC`, `CE`, `TI`, `PA`               |
| `documentNumber`  | string | sí           | no puede estar vacío y debe ser único       |

**Response:** `201 Created`

Header `Location: /api/v1/employees/{id}`

```json
{
  "id": 1,
  "fullName": "Ana Gómez",
  "email": "ana.gomez@example.com",
  "documentType": "CC",
  "documentNumber": "1020304050",
  "active": true
}
```

El campo `active` se fija en `true` automáticamente al crear el empleado; no se envía en el request.

### Errores posibles

**`400 Bad Request`** — datos inválidos (falta un campo requerido, email mal formado, etc.):

```json
{
  "timestamp": "2026-08-08T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Datos invalidos",
  "fieldErrors": {
    "email": "must be a well-formed email address"
  }
}
```

**`409 Conflict`** — el email o el número de documento ya están registrados:

```json
{
  "timestamp": "2026-08-08T10:00:00",
  "status": 409,
  "error": "Conflict",
  "message": "El email o el numero de documento ya estan registrados"
}
```
