# G3 Application

API REST de gestión administrativa de usuarios, desarrollada con Spring Boot. Incluye autenticación JWT, autorización por rol, contratos HTTP homogéneos, paginación y manejo centralizado de errores.

## Estado funcional

| Área | Estado |
|---|---|
| Persistencia, validación, paginación y búsquedas de usuarios | Implementado |
| Creación, actualización y restablecimiento administrativo de contraseña | Implementado |
| Autenticación mediante email y contraseña | Implementado |
| JWT firmado con HS256 y validador de emisor/caducidad | Implementado |
| Autorización de usuarios por `ROLE_ADMINISTRATOR` | Implementado |
| Respuestas JSON homogéneas para éxito y error | Implementado |
| Errores de seguridad `401` y `403` en JSON | Implementado |
| Pruebas de integración HTTP automatizadas | Pendiente |
| Refresh token, cierre de sesión y revocación de tokens | Pendiente |

## Requisitos y ejecución local

- Java 21
- Maven Wrapper incluido
- Una clave de firma JWT Base64 de 32 bytes

Genera una clave local y arranca la aplicación:

```bash
export JWT_SIGNING_KEY="$(openssl rand -base64 32)"
./mvnw spring-boot:run
```

La configuración actual usa H2 en memoria. El esquema se crea al arrancar y `data.sql` carga datos de ejemplo; sus usuarios usan la contraseña de desarrollo `password`. No deben utilizarse esos datos ni esa contraseña en producción.

Para ejecutar la suite disponible:

```bash
JWT_SIGNING_KEY="$(openssl rand -base64 32)" ./mvnw test
```

## Autenticación y autorización

La API no mantiene sesiones de servidor: el cliente obtiene un JWT y lo envía en cada petición protegida.

```http
Authorization: Bearer <accessToken>
```

`POST /api/auth/login` valida el email y la contraseña, y devuelve un token con:

- `iss`: emisor configurado (`g3`).
- `sub`: email normalizado del usuario.
- `role`: único rol del usuario, por ejemplo `ROLE_ADMINISTRATOR`.
- `iat` y `exp`: emisión y caducidad. La duración actual es de 15 minutos.

Los roles disponibles son:

```text
ROLE_ADMINISTRATOR
ROLE_INSPECTOR
ROLE_MAINTENANCE_TECHNICIAN
```

Las operaciones bajo `/api/users` requieren `ROLE_ADMINISTRATOR`. Un token ausente, inválido o caducado recibe `401`; un token válido sin ese rol recibe `403`.

## Endpoints

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| `GET` | `/actuator/health` | Público | Comprobación de salud para despliegue. |
| `POST` | `/api/auth/login` | Público | Inicia sesión y devuelve un JWT. |
| `GET` | `/api/users?page=0&size=20` | Administrador | Devuelve usuarios paginados por `id`. Tamaño máximo: `100`. |
| `GET` | `/api/users/by-email?email=ane&page=0&size=20` | Administrador | Busca usuarios cuyo email contiene el texto indicado, sin distinguir mayúsculas de minúsculas. |
| `GET` | `/api/users/by-status?enabled=true&page=0&size=20` | Administrador | Devuelve usuarios habilitados o deshabilitados de forma paginada. |
| `GET` | `/api/users/roles` | Administrador | Devuelve el catálogo de roles. |
| `GET` | `/api/users/{id}` | Administrador | Obtiene un usuario. |
| `POST` | `/api/users` | Administrador | Crea un usuario. |
| `PUT` | `/api/users/{id}` | Administrador | Actualiza datos administrativos, incluido estado y rol. |
| `PUT` | `/api/users/{id}/password` | Administrador | Restablece la contraseña. |

Ejemplo de inicio de sesión:

```json
{
  "email": "ane.garcia@example.com",
  "password": "password"
}
```

## Modelo de usuario

`User` persiste identificador, nombre, apellidos opcionales, email único, hash BCrypt, rol, estado y fechas de creación/actualización. La validación de entrada se concentra en los DTOs. El email se recorta y normaliza íntegramente a minúsculas.

La actualización administrativa no modifica la contraseña. El restablecimiento se realiza en su endpoint propio y el servicio guarda solamente el hash BCrypt. No hay borrado físico: `enabled` controla el acceso futuro del usuario.

Los nombres del enumerador `Role` se persisten y se exponen como código de API. Renombrar o eliminar una constante exige adaptar los datos persistidos y los clientes afectados.

## Contratos HTTP

Las respuestas correctas usan `ApiSuccessResponse`:

```json
{
  "data": {},
  "message": "Mensaje informativo.",
  "timestamp": "2026-11-03T22:00:00Z"
}
```

Los errores usan `ApiErrorResponse`:

```json
{
  "message": "La solicitud contiene datos inválidos.",
  "errors": {
    "email": ["La dirección de correo electrónico debe tener un formato válido."]
  },
  "timestamp": "2026-11-03T22:00:00Z"
}
```

`errors` es un mapa vacío cuando el error no se asocia a un campo o parámetro concreto.

## Errores gestionados

| Estado | Origen | Tratamiento |
|---|---|---|
| `400` | DTO, parámetros inválidos o ausentes, tipos incompatibles o JSON inválido | `GlobalExceptionHandler` devuelve mensajes de validación. |
| `401` | Credenciales de login inválidas | `GlobalExceptionHandler` devuelve un mensaje genérico. |
| `401` | JWT ausente, inválido o caducado | `RestAuthenticationEntryPoint` devuelve JSON desde la cadena de seguridad. |
| `403` | JWT válido sin permiso | `RestAccessDeniedHandler` devuelve JSON desde la cadena de seguridad. |
| `404` | Usuario inexistente | `UserNotFoundException`. |
| `405` | Método HTTP no admitido | Se conserva la cabecera `Allow`. |
| `406` | Cabecera `Accept` incompatible | Se conservan las representaciones admitidas. |
| `409` | Email ya asignado | `EmailAlreadyInUseException`. |
| `415` | `Content-Type` no admitido | Se conservan los tipos admitidos. |
| `500` | Error no previsto | Se registra internamente sin exponer detalles. |

## Postman

La colección [postman/G3Application.postman_collection.json](postman/G3Application.postman_collection.json) almacena automáticamente los tokens de administrador e inspector y permite comprobar `200`, `401` y `403`, además de las operaciones de usuarios.

Por defecto apunta a `http://localhost:8080`. Cambia la variable `baseUrl` para probar otro despliegue.

## Despliegue y límites conocidos

- El proyecto incluye `Dockerfile`, `.dockerignore` y `/actuator/health` para despliegues como Render.
- `JWT_SIGNING_KEY` es obligatoria y debe configurarse como secreto del entorno, nunca en Git.
- H2, `create-drop` y `data.sql` son solo adecuados para desarrollo. Producción requiere una base persistente, esquema gestionado manualmente y `spring.jpa.hibernate.ddl-auto=validate`.
- Un cambio de rol, deshabilitación o restablecimiento de contraseña no invalida los JWT ya emitidos: conservan sus permisos hasta su caducidad, actualmente 15 minutos.
- La comprobación previa de email duplicado no elimina una carrera concurrente. La restricción única de la base de datos protege la integridad, pero ese caso termina hoy como `500` genérico.
- Las contraseñas se validan actualmente por caracteres; queda pendiente limitar también su tamaño en bytes antes de BCrypt.
