# G3 Application

API REST desarrollada con Spring Boot para la gestión administrativa de usuarios. El estado actual
de `develop` cubre el módulo `user`: persistencia, validación, paginación, contratos HTTP y manejo
centralizado de errores.

## Estado actual

| Área | Estado |
|---|---|
| Entidad y persistencia de usuarios | Implementado |
| Creación, actualización y restablecimiento administrativo de contraseñas | Implementado |
| Listado paginado y catálogo de roles | Implementado |
| Respuestas de éxito y error | Implementado |
| Manejo global de errores HTTP | Implementado |
| Autenticación y autorización JWT | Pendiente |
| Pruebas de endpoints con MockMvc | Pendiente |

## Requisitos y ejecución

- Java 21
- Maven Wrapper incluido

Durante el desarrollo se utiliza H2 en memoria, el esquema se crea al arrancar y se cargan datos de
ejemplo desde `data.sql`.

```bash
./mvnw spring-boot:run
```

Para ejecutar la suite actual:

```bash
./mvnw test
```

## Módulo `user`

La entidad `User` contiene los siguientes datos:

| Campo | Descripción |
|---|---|
| `id` | Identificador persistente. |
| `name` | Nombre obligatorio. |
| `lastName` | Apellidos opcionales. |
| `email` | Dirección única, normalizada completamente a minúsculas. |
| `passwordHash` | Hash BCrypt; nunca se expone por la API. |
| `role` | Rol del usuario persistido como texto. |
| `enabled` | Estado de acceso del usuario. |
| `createdAt` / `updatedAt` | Marcas temporales gestionadas por JPA. |

Los DTO validan la entrada. El constructor y los métodos que alteran `User` tienen visibilidad de
paquete, de modo que los casos de uso del módulo controlan su creación y modificación.

### Roles disponibles

```text
ROLE_ADMINISTRATOR
ROLE_INSPECTOR
ROLE_MAINTENANCE_TECHNICIAN
```

Los nombres de los roles se persisten y se exponen como código de API. Renombrar o eliminar una
constante requerirá adaptar los datos persistidos.

### Política de actualización

`PUT /api/users/{id}` actualiza los datos administrativos del usuario: nombre, apellidos, email,
rol y estado. El email se normaliza a minúsculas. La contraseña se restablece de forma independiente
mediante `PUT /api/users/{id}/password`, donde se codifica con BCrypt antes de persistirse. No existe
borrado físico: se utiliza `enabled` para desactivar usuarios.

## Endpoints disponibles

> Mientras se implementa JWT, los endpoints de usuarios son públicos temporalmente y CSRF está
> desactivado. Esta configuración no es adecuada para producción.

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/users?page=0&size=20` | Devuelve usuarios paginados, ordenados por `id`. El tamaño máximo es `100`. |
| `GET` | `/api/users/roles` | Devuelve el catálogo de roles disponibles. |
| `GET` | `/api/users/{id}` | Obtiene un usuario por identificador. |
| `POST` | `/api/users` | Crea un usuario desde un rol administrativo. |
| `PUT` | `/api/users/{id}` | Actualiza los datos administrativos de un usuario, excepto su contraseña. |
| `PUT` | `/api/users/{id}/password` | Restablece la contraseña de un usuario desde un rol administrativo. |

El cuerpo de `POST` incluye:

```json
{
  "name": "Ane",
  "lastName": "García",
  "email": "ane.garcia@example.com",
  "password": "contraseña-segura",
  "role": "ROLE_INSPECTOR"
}
```

El cuerpo de `PUT /api/users/{id}` contiene:

```json
{
  "name": "Ane",
  "lastName": "García",
  "email": "ane.garcia@example.com",
  "role": "ROLE_INSPECTOR",
  "enabled": true
}
```

El cuerpo de `PUT /api/users/{id}/password` contiene únicamente la nueva contraseña:

```json
{
  "password": "contraseña-segura"
}
```

La creación inicial habilita el usuario por defecto.

## Contratos de respuesta

Las respuestas correctas usan `ApiSuccessResponse`:

```json
{
  "data": {},
  "message": "Mensaje informativo.",
  "timestamp": "2026-11-02T09:00:00Z"
}
```

Los errores usan `ApiErrorResponse`:

```json
{
  "message": "La solicitud contiene datos inválidos.",
  "errors": {
    "email": ["La dirección de correo electrónico debe tener un formato válido."]
  },
  "timestamp": "2026-11-02T09:00:00Z"
}
```

`errors` es un mapa vacío cuando el error no corresponde a campos o parámetros concretos.

## Manejo de errores HTTP

`GlobalExceptionHandler` transforma las excepciones conocidas en respuestas homogéneas.

| Estado | Caso |
|---|---|
| `400` | Validación de DTO, parámetros inválidos, tipos incompatibles o JSON inválido. |
| `404` | Usuario no encontrado. |
| `405` | Método HTTP no admitido; se conserva la cabecera `Allow`. |
| `406` | La cabecera `Accept` solicita un formato no disponible. |
| `409` | El email ya está asignado a otro usuario. |
| `415` | El `Content-Type` de la solicitud no es compatible. |
| `500` | Error interno no previsto, invariantes internas o carrera concurrente de email. |

La carrera concurrente de creación o actualización con el mismo email queda deliberadamente en el
tratamiento genérico `500`; la restricción única de base de datos preserva la integridad.

## Pendientes conocidos

- Validar la longitud de la contraseña en bytes antes de BCrypt.
- Añadir pruebas MockMvc para los estados `200`, `201`, `400`, `404`, `405`, `406`, `409`, `415` y `500`.
- Implementar autenticación JWT, autoridades y respuestas JSON de seguridad para `401` y `403`.
- Separar configuración de desarrollo y producción. Producción requerirá una base de datos
  persistente y `spring.jpa.hibernate.ddl-auto=validate`; el esquema se gestionará manualmente,
  sin herramienta de migraciones.
