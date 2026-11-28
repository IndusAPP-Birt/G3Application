# G3 Application

API REST para la gestión de usuarios, activos industriales, plantillas preventivas, su planificación por máquina y las ejecuciones de mantenimiento resultantes. La API utiliza JSON, validación de entrada, paginación homogénea y un contrato común de respuestas.

## Visión general

La aplicación se organiza alrededor de seis recursos principales:

```text
User
Machine
PreventivePlan
PreventivePlanAssignment
Preventive
Incident
```

Una máquina y una plantilla preventiva existen de forma independiente. `PreventivePlanAssignment` las vincula y configura el calendario de mantenimiento. Desde una asignación activa, el proceso programado genera ejecuciones `Preventive` que un inspector asignado puede iniciar, documentar y completar.

`Incident` representa una incidencia detectada durante la ejecución de un preventivo. Define su gravedad y ciclo de trabajo; actualmente permite comunicarla, asignarla a un técnico e iniciar su reparación.

## Documentación

| Recurso | Contenido |
|---|---|
| [Lógica de negocio](docs/logica-de-negocio_0.1.docx) | Entidades, relaciones, reglas verificadas, excepciones, pruebas y flujo completo de mantenimiento preventivo. |
| [Esquema de base de datos](docs/esquema-base-datos.svg) | Tablas persistentes, claves, restricciones y relaciones del modelo de datos. |
| [Auditoría técnica](docs/auditoria-aplicacion.md) | Hallazgos técnicos, prioridades y acciones de mejora. |
| [Colección Postman local](postman/G3Application.postman_collection.json) | Consultas contra el servicio local. |
| [Colección Postman Render](postman/G3Application_Render.postman_collection.json) | Consultas contra el servicio desplegado. |

## Convenciones de la API

- Los identificadores de recursos son positivos.
- Los listados paginados aceptan `page` y `size`. La página se numera desde cero; el tamaño predeterminado es `20` y el máximo es `100`.
- Los resultados paginados se ordenan ascendentemente por identificador para ofrecer páginas estables.
- Los filtros se exponen mediante rutas específicas y no se combinan entre sí.

## Autenticación y autorización

La API utiliza JWT firmados con HS256 y no mantiene sesión en servidor. El acceso se inicia mediante el endpoint público siguiente:

| Método | Ruta | Operación |
|---|---|---|
| `POST` | `/api/auth/login` | Valida las credenciales y devuelve un token de acceso JWT. |

Las operaciones protegidas requieren la cabecera:

```http
Authorization: Bearer <accessToken>
```

El token contiene el correo electrónico como sujeto y el rol en el claim `role`. Las rutas no declaradas se deniegan por defecto. `GET /actuator/health` y `POST /api/auth/login` son públicas.

| Recurso u operación | Administrador | Inspector | Técnico |
|---|:---:|:---:|:---:|
| Gestión de usuarios, plantillas y asignaciones | Sí | — | — |
| Perfil del usuario autenticado | Sí | Sí | Sí |
| Consulta de máquinas | Sí | Sí | Sí |
| Alta y actualización de máquinas | Sí | — | — |
| Consultas globales, asignación y bandeja de asignaciones preventivas | Sí | — | — |
| Bandeja propia de preventivos | — | Sí | — |
| Estados preventivos | Sí | Sí | — |
| Catálogos de estados y niveles de gravedad de incidencias | Sí | Sí | Sí |
| Listado y consulta individual de incidencias | Sí | — | — |
| Incidencias asociadas a un preventivo | Sí | Sí | — |
| Bandeja de incidencias asignadas | — | — | Sí |
| Incidencias comunicadas por el inspector | — | Sí | — |
| Filtros de incidencias por estado, gravedad, inspector o técnico | Sí | — | — |
| Comunicación y asignación de incidencias | — | Sí* | — |
| Inicio de incidencias asignadas | — | — | Sí* |
| Inicio, registro de trabajo y cierre de preventivos | — | Sí* | — |

`*` La capa de servicio comprueba además la relación del usuario autenticado con el recurso.

### Credenciales de desarrollo

Los usuarios cargados por `data.sql` comparten la contraseña `password`.

| Rol | Correo | Contraseña |
|---|---|---|
| Administrador | `ane.garcia@example.com` | `password` |
| Inspector | `kmarzana@ikaslea.birt.eus` | `password` |
| Técnico de mantenimiento | `unai.fernandez@example.com` | `password` |

## Recursos y endpoints

### Usuarios

Gestiona datos de acceso, roles y estado de habilitación de los usuarios.

| Método | Ruta | Operación |
|---|---|---|
| `GET` | `/api/users/roles` | Obtiene el catálogo de roles. |
| `GET` | `/api/users` | Obtiene usuarios paginados. |
| `GET` | `/api/users/{id}` | Obtiene un usuario por identificador. |
| `GET` | `/api/users/me` | Obtiene el perfil del usuario autenticado. |
| `GET` | `/api/users/by-email` | Filtra usuarios por coincidencia parcial de correo. |
| `GET` | `/api/users/by-status` | Filtra usuarios por estado de acceso. |
| `GET` | `/api/users/by-role` | Filtra usuarios por rol. |
| `POST` | `/api/users` | Crea un usuario. |
| `PUT` | `/api/users/{id}` | Actualiza los datos administrativos de un usuario. |
| `PUT` | `/api/users/{id}/password` | Restablece la contraseña de un usuario. |

### Máquinas

Representa los activos industriales de planta y su estado operativo.

| Método | Ruta | Operación |
|---|---|---|
| `GET` | `/api/machines/statuses` | Obtiene el catálogo de estados operativos. |
| `GET` | `/api/machines` | Obtiene máquinas paginadas. |
| `GET` | `/api/machines/{id}` | Obtiene una máquina por identificador. |
| `GET` | `/api/machines/by-asset-code` | Filtra por coincidencia parcial de código patrimonial. |
| `GET` | `/api/machines/by-serial-number` | Filtra por coincidencia parcial de número de serie. |
| `GET` | `/api/machines/by-status` | Filtra por estado operativo. |
| `POST` | `/api/machines` | Registra una máquina. |
| `PUT` | `/api/machines/{id}` | Actualiza los datos ordinarios de una máquina. |
| `PUT` | `/api/machines/{id}/asset-code` | Corrige el código patrimonial. |

### Plantillas preventivas

Define instrucciones de mantenimiento reutilizables e independientes de las máquinas.

| Método | Ruta | Operación |
|---|---|---|
| `GET` | `/api/preventive-plans` | Obtiene plantillas paginadas. |
| `GET` | `/api/preventive-plans/{id}` | Obtiene una plantilla por identificador. |
| `GET` | `/api/preventive-plans/by-status` | Filtra plantillas por estado de activación. |
| `GET` | `/api/preventive-plans/by-name` | Filtra por coincidencia parcial de nombre. |
| `POST` | `/api/preventive-plans` | Crea una plantilla preventiva. |
| `PUT` | `/api/preventive-plans/{id}` | Actualiza el contenido y la activación de una plantilla. |

### Asignaciones preventivas

Vincula una plantilla con una máquina y establece su calendario específico de mantenimiento.

| Método | Ruta | Operación |
|---|---|---|
| `GET` | `/api/preventive-plan-assignments` | Obtiene asignaciones paginadas. |
| `GET` | `/api/preventive-plan-assignments/{id}` | Obtiene una asignación por identificador. |
| `GET` | `/api/preventive-plan-assignments/by-status` | Filtra asignaciones por activación. |
| `GET` | `/api/preventive-plan-assignments/by-machine/{machineId}` | Filtra asignaciones por máquina. |
| `GET` | `/api/preventive-plan-assignments/by-preventive-plan/{preventivePlanId}` | Filtra asignaciones por plantilla. |
| `POST` | `/api/preventive-plan-assignments` | Asigna una plantilla a una máquina con su calendario inicial. |
| `PUT` | `/api/preventive-plan-assignments/{id}` | Actualiza el calendario y la activación. |

### Mantenimientos preventivos

Representa las ejecuciones generadas desde una asignación preventiva y su ciclo de trabajo. Al asignar un inspector a un preventivo, la aplicación le envía una notificación por correo y comunica la asignación al chat central configurado de Telegram.

| Método | Ruta | Operación |
|---|---|---|
| `GET` | `/api/preventives` | Obtiene preventivos paginados. |
| `GET` | `/api/preventives/statuses` | Obtiene el catálogo de estados de ejecución. |
| `GET` | `/api/preventives/{id}` | Obtiene un preventivo por identificador. |
| `GET` | `/api/preventives/by-plan/{preventivePlanId}` | Filtra preventivos por plantilla de origen. |
| `GET` | `/api/preventives/by-status` | Filtra preventivos por estado. |
| `GET` | `/api/preventives/by-inspector/{inspectorId}` | Filtra preventivos por inspector asignado. |
| `GET` | `/api/preventives/me` | Obtiene la bandeja del inspector autenticado. |
| `GET` | `/api/preventives/assigned-by-me` | Obtiene los preventivos asignados por el administrador autenticado. |
| `GET` | `/api/preventives/by-due-date` | Filtra por un rango inclusivo de fecha prevista. |
| `PUT` | `/api/preventives/{id}/assignment` | Asigna un inspector a un preventivo pendiente. |
| `PUT` | `/api/preventives/{id}/start` | Inicia un preventivo asignado. |
| `PUT` | `/api/preventives/{id}/work-performed` | Registra o corrige el trabajo realizado. |
| `PUT` | `/api/preventives/{id}/completion` | Completa un preventivo con trabajo registrado. |

### Incidencias

Representa los problemas detectados durante la ejecución de un preventivo. Permite comunicar incidencias en preventivos en curso, asignarlas a un técnico habilitado e iniciar su reparación, además de consultar sus catálogos, listado paginado, detalle, incidencias por preventivo y bandejas personales. El listado, el detalle y los filtros por estado, gravedad, inspector comunicador o técnico asignado están reservados a administración; la consulta por preventivo está disponible para administración e inspección. Al asignar un técnico, la aplicación le envía una notificación por correo tras confirmar la transacción.

| Método | Ruta | Operación |
|---|---|---|
| `GET` | `/api/incidents/statuses` | Obtiene el catálogo de estados de incidencias. |
| `GET` | `/api/incidents/severities` | Obtiene el catálogo de niveles de gravedad de incidencias. |
| `GET` | `/api/incidents` | Obtiene incidencias paginadas. |
| `GET` | `/api/incidents/{id}` | Obtiene una incidencia por identificador. |
| `GET` | `/api/preventives/{preventiveId}/incidents` | Obtiene las incidencias paginadas asociadas a un preventivo. |
| `GET` | `/api/incidents/me` | Obtiene las incidencias paginadas asignadas al técnico autenticado. |
| `GET` | `/api/incidents/reported-by-me` | Obtiene las incidencias paginadas comunicadas por el inspector autenticado. |
| `GET` | `/api/incidents/by-status` | Filtra incidencias por estado. |
| `GET` | `/api/incidents/by-severity` | Filtra incidencias por nivel de gravedad. |
| `GET` | `/api/incidents/by-reporter/{inspectorId}` | Filtra incidencias por inspector comunicador. |
| `GET` | `/api/incidents/by-technician/{technicianId}` | Filtra incidencias por técnico asignado. |
| `POST` | `/api/preventives/{preventiveId}/incidents` | Comunica una incidencia en un preventivo en progreso. |
| `PUT` | `/api/incidents/{id}/assignment` | Asigna un técnico habilitado a una incidencia abierta. |
| `PUT` | `/api/incidents/{id}/start` | Inicia la reparación de una incidencia asignada. |

El flujo disponible es `OPEN → ASSIGNED → IN_PROGRESS`: el inspector asignado al preventivo comunica la incidencia; el inspector que la comunicó asigna el técnico; y el técnico asignado inicia la reparación.

## Colecciones Postman

Las colecciones incluyen las rutas publicadas, variables de identificadores, filtros, rangos de fechas y token de acceso.

| Archivo | Destino | Variable `baseUrl` |
|---|---|---|
| `postman/G3Application.postman_collection.json` | Desarrollo local | `http://localhost:8080` |
| `postman/G3Application_Render.postman_collection.json` | Servicio desplegado | URL pública del servicio |

Cada colección comienza con `Autenticación > Iniciar sesión`, que guarda el JWT en la variable `accessToken` para las peticiones protegidas. Para el flujo de incidencias, usa un token de inspector al comunicar o asignar y uno del técnico asignado al iniciar la reparación.

## Configuración operativa

| Propiedad | Variable de entorno | Finalidad |
|---|---|---|
| `security.jwt.signing-key` | `JWT_SIGNING_KEY` | Clave Base64 utilizada para firmar JWT. |
| `spring.mail.host` | `MAIL_HOST` | Servidor SMTP. |
| `spring.mail.port` | `MAIL_PORT` | Puerto SMTP. |
| `spring.mail.username` | `MAIL_USERNAME` | Cuenta remitente. |
| `spring.mail.password` | `MAIL_PASSWORD` | Credencial o contraseña de aplicación SMTP. |
| `telegram.bot-token` | `TELEGRAM_BOT_TOKEN` | Token del bot que envía las notificaciones centrales. |
| `telegram.chat-id` | `TELEGRAM_CHAT_ID` | Identificador del chat central destinatario de las notificaciones. |
| `preventive.generation.cron` | `PREVENTIVE_GENERATION_CRON` | Programación de la generación preventiva. |
| `preventive.generation.zone` | `PREVENTIVE_GENERATION_ZONE` | Zona horaria de la programación; predeterminada `Europe/Madrid`. |

La programación predeterminada revisa las asignaciones todos los días a las 06:00 en la zona `Europe/Madrid`.

## Observabilidad

| Método | Ruta | Operación |
|---|---|---|
| `GET` | `/actuator/health` | Comprueba la disponibilidad del servicio. |

## Contratos de respuesta

Las respuestas correctas incluyen los siguientes campos:

| Campo | Tipo | Descripción |
|---|---|---|
| `data` | Objeto, lista o página | Resultado del recurso u operación solicitada. |
| `message` | Texto | Descripción legible del resultado. |
| `timestamp` | Fecha y hora ISO-8601 | Momento en que se construye la respuesta. |

Una respuesta paginada contiene, dentro de `data`, el contenido solicitado y su metadato de paginación: número de página, tamaño, total de elementos y total de páginas.

Las respuestas de error incluyen:

| Campo | Tipo | Descripción |
|---|---|---|
| `message` | Texto | Descripción legible del error. |
| `errors` | Mapa de listas de texto | Errores asociados a campos o parámetros, cuando procede. |
| `timestamp` | Fecha y hora ISO-8601 | Momento en que se construye la respuesta. |

`errors` puede estar vacío cuando el error no pertenece a un campo o parámetro concreto.

## Manejo de errores HTTP

| Estado | Caso |
|---|---|
| `400` | Validación de solicitudes, parámetros ausentes o incompatibles, formato inválido, rango de fechas no válido, ausencia de trabajo registrado o asignación de un usuario sin rol de inspector. |
| `401` | Petición a una ruta protegida sin autenticación válida. |
| `403` | Usuario autenticado sin autorización para la operación solicitada. |
| `404` | Recurso solicitado no encontrado. |
| `405` | Método HTTP no admitido; se conserva la cabecera `Allow`. |
| `406` | Representación no disponible para la cabecera `Accept` solicitada. |
| `409` | Conflicto con un valor que debe ser único o transición de estado no permitida. |
| `415` | Tipo de contenido no compatible. |
| `500` | Error interno no previsto. |

## Lógica de negocio

La descripción completa de entidades, relaciones, reglas verificadas, excepciones, pruebas y flujo de mantenimiento preventivo está disponible en [Lógica de negocio](docs/logica-de-negocio_0.1.docx).
