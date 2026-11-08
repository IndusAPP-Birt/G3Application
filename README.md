# G3 Application

API REST desarrollada con Spring Boot para la gestión administrativa de usuarios y maquinaria
industrial. El estado actual de la rama `develop` cubre ambos módulos: persistencia, validación,
paginación, contratos HTTP y manejo centralizado de errores.

## Estado actual

| Área | Estado |
|---|---|
| Entidad, persistencia y operaciones administrativas de usuarios | Implementado |
| Filtros paginados de usuarios por email, estado y rol | Implementado |
| Entidad, persistencia y operaciones administrativas de máquinas | Implementado |
| Filtros paginados de máquinas por código, número de serie y estado | Implementado |
| Respuestas de éxito y error | Implementado |
| Manejo global de errores HTTP | Implementado |
| Planes y tareas preventivas | Pendiente |
| Autenticación y autorización JWT | Pendiente en `develop`; disponible en `feat/auth` |
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

La entidad `User` contiene nombre, apellidos opcionales, email único, hash BCrypt de contraseña,
rol, estado de acceso y marcas temporales. El email se normaliza completamente a minúsculas y el
hash nunca se expone por la API.

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

`PUT /api/users/{id}` actualiza nombre, apellidos, email, rol y estado de acceso. La contraseña se
restablece independientemente mediante `PUT /api/users/{id}/password`, donde se codifica con BCrypt
antes de persistirse. No existe borrado físico: se utiliza `enabled` para desactivar usuarios.

## Módulo `machine`

La entidad `Machine` identifica maquinaria industrial mediante un código patrimonial único e
incluye nombre, fabricante, modelo, número de serie opcional, ubicación, fecha de puesta en
servicio, estado operativo y marcas temporales.

```text
ACTIVE
INACTIVE
RETIRED
```

- El código patrimonial se normaliza a mayúsculas y se corrige exclusivamente mediante su caso de
  uso específico.
- El número de serie es opcional, pero único cuando se informa.
- La fecha de puesta en servicio no puede ser futura y permanece inmutable tras el registro.
- No existe borrado físico: una máquina deja de operar mediante el estado `RETIRED`.

## Autenticación y autorización

La rama `develop` todavía no incorpora autenticación ni autorización JWT. Durante esta fase, los
endpoints bajo `/api/users` y `/api/machines` son públicos y CSRF está desactivado. Esta
configuración es temporal y no es adecuada para producción.

La rama `feat/auth` contiene la variante que requiere un JWT válido con rol de administrador para
acceder a las operaciones administrativas.

## Endpoints disponibles

Todos los parámetros `page` se basan en cero. El tamaño por defecto es `20` y el máximo es `100`.

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/actuator/health` | Comprobación de salud pública para despliegue. |

### Usuarios

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/users/roles` | Devuelve el catálogo de roles disponibles. |
| `GET` | `/api/users?page=0&size=20` | Devuelve usuarios paginados, ordenados por `id`. |
| `GET` | `/api/users/{id}` | Obtiene un usuario por identificador. |
| `GET` | `/api/users/by-email?email=ane&page=0&size=20` | Busca usuarios cuyo email contiene el texto indicado, sin distinguir mayúsculas de minúsculas. |
| `GET` | `/api/users/by-status?enabled=true&page=0&size=20` | Devuelve usuarios habilitados o deshabilitados de forma paginada. |
| `GET` | `/api/users/by-role?role=ROLE_INSPECTOR&page=0&size=20` | Devuelve usuarios con el rol indicado de forma paginada. |
| `POST` | `/api/users` | Crea un usuario desde una solicitud administrativa. |
| `PUT` | `/api/users/{id}` | Actualiza los datos administrativos de un usuario, excepto su contraseña. |
| `PUT` | `/api/users/{id}/password` | Restablece la contraseña de un usuario desde una solicitud administrativa. |

### Máquinas

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/machines/statuses` | Devuelve el catálogo de estados operativos. |
| `GET` | `/api/machines?page=0&size=20` | Devuelve máquinas paginadas, ordenadas por `id`. |
| `GET` | `/api/machines/{id}` | Obtiene una máquina por identificador. |
| `GET` | `/api/machines/by-asset-code?assetCode=MAQ&page=0&size=20` | Busca máquinas cuyo código patrimonial contiene el texto indicado. |
| `GET` | `/api/machines/by-serial-number?serialNumber=FAG&page=0&size=20` | Busca máquinas cuyo número de serie contiene el texto indicado. |
| `GET` | `/api/machines/by-status?status=ACTIVE&page=0&size=20` | Devuelve máquinas con el estado indicado de forma paginada. |
| `POST` | `/api/machines` | Registra una máquina. |
| `PUT` | `/api/machines/{id}` | Actualiza los datos administrativos ordinarios de una máquina. |
| `PUT` | `/api/machines/{id}/asset-code` | Corrige el código patrimonial de una máquina. |

Los filtros de cada módulo se exponen mediante rutas independientes; no se combinan entre sí. Un
parámetro de consulta no declarado por un endpoint se ignora.

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
    "email": ["La dirección de correo electrónico es obligatoria."]
  },
  "timestamp": "2026-11-02T09:00:00Z"
}
```

`errors` es un mapa vacío cuando el error no corresponde a campos o parámetros concretos.

## Manejo de errores HTTP

`GlobalExceptionHandler` transforma las excepciones conocidas en respuestas homogéneas.

| Estado | Caso |
|---|---|
| `400` | Validación de DTO, parámetros inválidos o ausentes, tipos incompatibles o JSON inválido. |
| `404` | Usuario o máquina no encontrados. |
| `405` | Método HTTP no admitido; se conserva la cabecera `Allow`. |
| `406` | La cabecera `Accept` solicita un formato no disponible. |
| `409` | Email, código patrimonial o número de serie ya asignados. |
| `415` | El `Content-Type` de la solicitud no es compatible. |
| `500` | Error interno no previsto, invariantes internas o carrera concurrente sobre un valor único. |

Las carreras concurrentes sobre valores únicos no tienen tratamiento específico por decisión de
alcance. La restricción única de base de datos preserva la integridad y la respuesta resultante se
mantiene como error interno genérico.

## Postman

Existen colecciones para el entorno local y Render con la comprobación de salud y todos los
endpoints actuales de usuarios y máquinas:

- [Entorno local](postman/G3Application.postman_collection.json): `http://localhost:8080`.
- [Render](postman/G3Application_Render.postman_collection.json): `https://g3application.onrender.com`.

## Pendientes conocidos

- Definir e implementar planes preventivos y sus tareas asociadas.
- Añadir pruebas MockMvc para los escenarios HTTP principales de usuarios y máquinas.
- Validar la longitud de la contraseña en bytes antes de BCrypt.
- Implementar autenticación JWT, autoridades y respuestas JSON de seguridad para `401` y `403`.
- Separar configuración de desarrollo y producción. Producción requerirá una base de datos
  persistente y `spring.jpa.hibernate.ddl-auto=validate`; el esquema se gestionará manualmente,
  sin herramienta de migraciones.
