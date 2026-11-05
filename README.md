# G3 Application

API REST desarrollada con Spring Boot para la gestión administrativa de usuarios. Esta rama `feat/auth` amplía el módulo `user` con autenticación JWT y autorización por rol.

## Estado actual

| Área | Estado |
|---|---|
| Entidad y persistencia de usuarios | Implementado |
| Creación, actualización y restablecimiento administrativo de contraseñas | Implementado |
| Listado paginado, búsquedas de usuarios y catálogo de roles | Implementado |
| Respuestas de éxito y error | Implementado |
| Manejo global de errores HTTP | Implementado |
| Autenticación mediante email y contraseña | Implementado |
| JWT firmado con HS256 y validación de emisor/caducidad | Implementado |
| Autorización de usuarios por `ROLE_ADMINISTRATOR` | Implementado |
| Pruebas de endpoints con MockMvc | Pendiente |
| Refresh token, cierre de sesión y revocación de tokens | Pendiente |

## Requisitos y ejecución

- Java 21
- Maven Wrapper incluido
- Una clave de firma JWT Base64 de 32 bytes

Durante el desarrollo se utiliza H2 en memoria, el esquema se crea al arrancar y se cargan datos de ejemplo desde `data.sql`. Sus usuarios usan la contraseña de desarrollo `password`.

```bash
export JWT_SIGNING_KEY="$(openssl rand -base64 32)"
./mvnw spring-boot:run
```

Para ejecutar la suite actual:

```bash
JWT_SIGNING_KEY="$(openssl rand -base64 32)" ./mvnw test
```

## Módulo `user`

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

Los DTO validan la entrada. El constructor y los métodos que alteran `User` tienen visibilidad de paquete, de modo que los casos de uso del módulo controlan su creación y modificación.

### Roles disponibles

```text
ROLE_ADMINISTRATOR
ROLE_INSPECTOR
ROLE_MAINTENANCE_TECHNICIAN
```

Los nombres de los roles se persisten y se exponen como código de API. Renombrar o eliminar una constante requerirá adaptar los datos persistidos.

### Política de actualización

`PUT /api/users/{id}` actualiza nombre, apellidos, email, rol y estado. El email se normaliza a minúsculas. La contraseña se restablece por separado mediante `PUT /api/users/{id}/password`, donde se codifica con BCrypt. No existe borrado físico: se utiliza `enabled` para desactivar usuarios.

## Autenticación y autorización

`POST /api/auth/login` valida el email y la contraseña y devuelve un JWT de acceso de 15 minutos. El token incluye emisor, email normalizado como sujeto, rol y fechas de emisión y caducidad.

La API es sin estado: no conserva sesiones de servidor. El cliente debe enviar el token en cada petición protegida:

```http
Authorization: Bearer <accessToken>
```

`/actuator/health` y `POST /api/auth/login` son públicos. Todas las operaciones bajo `/api/users` requieren un JWT válido con `ROLE_ADMINISTRATOR`. Un token ausente, inválido o caducado devuelve `401`; un token válido sin la autoridad requerida devuelve `403`.

## Endpoints disponibles

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/actuator/health` | Comprobación de salud pública para despliegue. |
| `POST` | `/api/auth/login` | Inicio de sesión público; devuelve un JWT. |
| `GET` | `/api/users?page=0&size=20` | Devuelve usuarios paginados, ordenados por `id`. Tamaño máximo: `100`. Requiere administrador. |
| `GET` | `/api/users/by-email?email=ane&page=0&size=20` | Busca usuarios cuyo email contiene el texto indicado, sin distinguir mayúsculas de minúsculas. Requiere administrador. |
| `GET` | `/api/users/by-status?enabled=true&page=0&size=20` | Devuelve usuarios habilitados o deshabilitados de forma paginada. Requiere administrador. |
| `GET` | `/api/users/roles` | Devuelve el catálogo de roles disponibles. Requiere administrador. |
| `GET` | `/api/users/{id}` | Obtiene un usuario por identificador. Requiere administrador. |
| `POST` | `/api/users` | Crea un usuario desde un rol administrativo. Requiere administrador. |
| `PUT` | `/api/users/{id}` | Actualiza los datos administrativos de un usuario, excepto su contraseña. Requiere administrador. |
| `PUT` | `/api/users/{id}/password` | Restablece la contraseña de un usuario desde un rol administrativo. Requiere administrador. |

El cuerpo de `POST /api/auth/login` incluye:

```json
{
  "email": "ane.garcia@example.com",
  "password": "password"
}
```

El cuerpo de `POST /api/users` incluye:

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

`GlobalExceptionHandler` transforma las excepciones de la aplicación en respuestas homogéneas. Los errores de JWT se producen antes de llegar al controlador y los componentes de seguridad los traducen al mismo contrato JSON.

| Estado | Caso |
|---|---|
| `400` | Validación de DTO, parámetros inválidos o ausentes, tipos incompatibles o JSON inválido. |
| `401` | Credenciales de inicio de sesión inválidas, JWT ausente, inválido o caducado. |
| `403` | JWT válido sin permisos para la operación solicitada. |
| `404` | Usuario no encontrado. |
| `405` | Método HTTP no admitido; se conserva la cabecera `Allow`. |
| `406` | La cabecera `Accept` solicita un formato no disponible. |
| `409` | El email ya está asignado a otro usuario. |
| `415` | El `Content-Type` de la solicitud no es compatible. |
| `500` | Error interno no previsto, invariantes internas o carrera concurrente de email. |

La carrera concurrente de creación o actualización con el mismo email queda deliberadamente en el tratamiento genérico `500`; la restricción única de base de datos preserva la integridad.

## Postman

Las dos colecciones inician sesión como administrador e inspector, guardan sus tokens automáticamente y permiten probar la autorización `200`, `401` y `403`, además de las búsquedas por email parcial y estado:

- [Entorno local](postman/G3Application.postman_collection.json): `http://localhost:8080`.
- [Render con JWT](postman/G3Application_Render.postman_collection.json): `https://g3applicationjwt.onrender.com`.

## Pendientes conocidos

- Validar la longitud de la contraseña en bytes antes de BCrypt.
- Añadir pruebas MockMvc para los estados `200`, `201`, `400`, `401`, `403`, `404`, `405`, `406`, `409`, `415` y `500`.
- Implementar refresh token, cierre de sesión o revocación si se necesita invalidar un token antes de su caducidad.
- Separar configuración de desarrollo y producción. Producción requerirá una base de datos persistente y `spring.jpa.hibernate.ddl-auto=validate`; el esquema se gestionará manualmente, sin herramienta de migraciones.
