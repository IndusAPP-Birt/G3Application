# G3 Application

API REST para la administración de usuarios, activos industriales, plantillas preventivas, su
planificación por máquina y sus ejecuciones. La API emplea recursos JSON, validación de entrada,
paginación homogénea y respuestas con un contrato común.

## Convenciones de la API

- Los identificadores de recursos son positivos.
- Los listados paginados aceptan `page` y `size`. La página se numera desde cero; el tamaño por
  defecto es `20` y el máximo es `100`.
- Los resultados paginados se ordenan ascendentemente por identificador para ofrecer páginas
  estables.
- Los filtros se exponen mediante rutas específicas y no se combinan entre sí.

## Autenticación y autorización

La API utiliza JWT firmados con HS256 y no mantiene sesión en servidor. Para iniciar sesión se
envían las credenciales al endpoint público de autenticación:

| Método | Ruta | Operación |
|---|---|---|
| `POST` | `/api/auth/login` | Valida las credenciales y devuelve un token de acceso JWT. |

Las operaciones protegidas requieren la cabecera:

```http
Authorization: Bearer <accessToken>
```

La clave de firma se configura exclusivamente mediante `JWT_SIGNING_KEY`, codificada en Base64.
Los tokens incluyen el email como sujeto y el rol del usuario en el claim `role`.

| Recurso | Administrador | Inspector | Técnico |
|---|:---:|:---:|:---:|
| Gestión de usuarios, plantillas y asignaciones | Sí | — | — |
| Perfil del usuario autenticado | Sí | Sí | Sí |
| Consulta de máquinas | Sí | Sí | Sí |
| Alta y actualización de máquinas | Sí | — | — |
| Consultas globales y asignación de preventivos | Sí | — | — |
| Estados preventivos | Sí | Sí | — |
| Inicio, registro de trabajo y cierre de preventivos | — | Sí* | — |

`*` La capa de servicio debe comprobar además que el preventivo pertenece al inspector autenticado.
Las rutas no declaradas se deniegan por defecto. `GET /actuator/health`, `POST /api/auth/login` y
`GET /g3-console.html` permanecen públicos.

## Consola de pruebas

La aplicación publica una consola web ligera en `/g3-console.html`. Permite consultar, filtrar,
crear y actualizar los recursos disponibles desde el mismo origen que la API. Conserva la última
respuesta de cada operación durante la sesión de la página e incluye la paginación en el área de
resultados. La pestaña `Auth` obtiene el JWT y lo conserva en `sessionStorage` para adjuntarlo a
las operaciones protegidas; `Permisos` muestra la matriz detallada aplicada por la API.

Al desplegar la aplicación, la consola queda disponible en la misma URL pública del servicio; por
ejemplo, `https://servicio.onrender.com/g3-console.html`.

## Usuarios

Un usuario contiene nombre, apellidos opcionales, correo electrónico, contraseña protegida con
BCrypt, rol, estado de acceso y marcas temporales. El correo se normaliza a minúsculas y su hash
no forma parte de las respuestas públicas.

### Roles

```text
ROLE_ADMINISTRATOR
ROLE_INSPECTOR
ROLE_MAINTENANCE_TECHNICIAN
```

### Endpoints

| Método | Ruta | Operación |
|---|---|---|
| `GET` | `/api/users/roles` | Obtiene el catálogo de roles. |
| `GET` | `/api/users` | Obtiene usuarios paginados. |
| `GET` | `/api/users/{id}` | Obtiene un usuario por identificador. |
| `GET` | `/api/users/me` | Obtiene el perfil del usuario autenticado. |
| `GET` | `/api/users/by-email` | Filtra usuarios por coincidencia parcial de correo. |
| `GET` | `/api/users/by-status` | Filtra usuarios por estado de acceso. |
| `GET` | `/api/users/by-role` | Filtra usuarios por rol. |
| `POST` | `/api/users` | Crea un usuario administrativo. |
| `PUT` | `/api/users/{id}` | Actualiza los datos administrativos de un usuario. |
| `PUT` | `/api/users/{id}/password` | Restablece la contraseña de un usuario. |

La contraseña se actualiza mediante un recurso específico. La desactivación se gestiona mediante
el campo `enabled`, sin borrado físico.

## Máquinas

Una máquina se identifica mediante un código patrimonial único e incluye datos de fabricación,
serie, ubicación, fecha de puesta en servicio, estado operativo y marcas temporales.

```text
ACTIVE
INACTIVE
RETIRED
```

- El código patrimonial se normaliza a mayúsculas y se modifica mediante un caso de uso dedicado.
- El número de serie es opcional y único cuando se informa.
- La fecha de puesta en servicio es inmutable.
- El estado `RETIRED` conserva el histórico de la máquina.

### Endpoints

| Método | Ruta | Operación |
|---|---|---|
| `GET` | `/api/machines/statuses` | Obtiene el catálogo de estados operativos. |
| `GET` | `/api/machines` | Obtiene máquinas paginadas. |
| `GET` | `/api/machines/{id}` | Obtiene una máquina por identificador. |
| `GET` | `/api/machines/by-asset-code` | Filtra por coincidencia parcial de código patrimonial. |
| `GET` | `/api/machines/by-serial-number` | Filtra por coincidencia parcial de número de serie. |
| `GET` | `/api/machines/by-status` | Filtra por estado operativo. |
| `POST` | `/api/machines` | Registra una máquina. |
| `PUT` | `/api/machines/{id}` | Actualiza los datos administrativos ordinarios. |
| `PUT` | `/api/machines/{id}/asset-code` | Corrige el código patrimonial. |

## Planes preventivos

Un plan preventivo es una plantilla independiente de las máquinas. Define el nombre y las
acciones o instrucciones de mantenimiento que se describen en su contenido. Puede existir sin
estar asignado a ningún activo y reutilizarse en varias máquinas.

Su estado de activación controla si la plantilla puede utilizarse para nuevas asignaciones. No
incluye intervalos, avisos ni fechas de ejecución: esa configuración corresponde a la asignación.

### Endpoints

| Método | Ruta | Operación |
|---|---|---|
| `GET` | `/api/preventive-plans` | Obtiene planes preventivos paginados. |
| `GET` | `/api/preventive-plans/{id}` | Obtiene un plan preventivo por identificador. |
| `GET` | `/api/preventive-plans/by-status` | Filtra planes por estado de activación. |
| `GET` | `/api/preventive-plans/by-name` | Filtra por coincidencia parcial de nombre. |
| `POST` | `/api/preventive-plans` | Crea un plan preventivo activo. |
| `PUT` | `/api/preventive-plans/{id}` | Actualiza el contenido y el estado de activación. |

## Asignaciones de planes preventivos

Una asignación preventiva relaciona exactamente una plantilla y una máquina. Es la entidad que
define cómo se aplica el plan a ese activo concreto:

```text
PreventivePlanAssignment
├── plantilla preventiva
├── máquina
├── intervalDays
├── noticeDays
├── nextDueDate
└── active
```

Una máquina puede tener varias asignaciones y una plantilla puede asignarse a varias máquinas.
Solo puede existir una asignación por cada pareja máquina–plantilla. La plantilla y la máquina son
inmutables una vez creada la asignación; el calendario y su activación sí se pueden actualizar.

| Método | Ruta | Operación |
|---|---|---|
| `GET` | `/api/preventive-plan-assignments` | Obtiene asignaciones paginadas. |
| `GET` | `/api/preventive-plan-assignments/{id}` | Obtiene una asignación por identificador. |
| `GET` | `/api/preventive-plan-assignments/by-status` | Filtra asignaciones por activación. |
| `GET` | `/api/preventive-plan-assignments/by-machine/{machineId}` | Filtra asignaciones por máquina. |
| `GET` | `/api/preventive-plan-assignments/by-preventive-plan/{preventivePlanId}` | Filtra asignaciones por plantilla. |
| `POST` | `/api/preventive-plan-assignments` | Asigna una plantilla a una máquina con su calendario inicial. |
| `PUT` | `/api/preventive-plan-assignments/{id}` | Actualiza el calendario y la activación. |

## Ejecuciones preventivas

Un preventivo representa una ejecución concreta generada desde una asignación. Conserva la
asignación de origen, la fecha prevista y una instantánea inmutable del nombre y la descripción
de la plantilla. Así se preserva el contexto histórico aunque la plantilla se modifique después.

```text
PENDING → ASSIGNED → IN_PROGRESS → COMPLETED
```

- Solo se asigna un usuario con rol `ROLE_INSPECTOR` a un preventivo pendiente.
- Solo se inicia un preventivo asignado.
- El trabajo realizado se puede registrar o corregir mientras la ejecución está en progreso.
- Solo se completa un preventivo en progreso que tenga trabajo registrado.

### Endpoints

| Método | Ruta | Operación |
|---|---|---|
| `GET` | `/api/preventives` | Obtiene preventivos paginados. |
| `GET` | `/api/preventives/statuses` | Obtiene el catálogo de estados de ejecución. |
| `GET` | `/api/preventives/{id}` | Obtiene un preventivo por identificador. |
| `GET` | `/api/preventives/by-plan/{preventivePlanId}` | Filtra preventivos por plantilla de origen. |
| `GET` | `/api/preventives/by-status` | Filtra preventivos por estado. |
| `GET` | `/api/preventives/by-inspector/{inspectorId}` | Filtra preventivos por inspector asignado. |
| `GET` | `/api/preventives/by-due-date` | Filtra por un rango inclusivo de fecha prevista. |
| `PUT` | `/api/preventives/{id}/assignment` | Asigna un inspector a un preventivo pendiente. |
| `PUT` | `/api/preventives/{id}/start` | Inicia un preventivo asignado. |
| `PUT` | `/api/preventives/{id}/work-performed` | Registra o corrige el trabajo realizado. |
| `PUT` | `/api/preventives/{id}/completion` | Completa un preventivo con trabajo registrado. |

### Generación automática

Un proceso programado revisa las asignaciones activas por lotes de hasta cien elementos. Para cada
asignación, genera un preventivo pendiente cuando su próxima fecha prevista entra en la ventana de
aviso:

```text
nextDueDate ≤ fecha actual + noticeDays
```

Tras crear la ejecución, la asignación avanza su próxima fecha prevista según `intervalDays`. La
restricción formada por asignación y fecha prevista impide duplicar una misma ejecución.

La programación se configura mediante propiedades, que admiten sobrescritura por variables de
entorno:

| Propiedad | Variable de entorno | Valor predeterminado |
|---|---|---|
| `preventive.generation.cron` | `PREVENTIVE_GENERATION_CRON` | Todos los días a las 06:00. |
| `preventive.generation.zone` | `PREVENTIVE_GENERATION_ZONE` | `Europe/Madrid`. |

## Observabilidad

| Método | Ruta | Operación |
|---|---|---|
| `GET` | `/actuator/health` | Comprueba la disponibilidad del servicio. |

## Colecciones Postman

Las colecciones de Postman incluyen todos los endpoints publicados y variables para sus
identificadores, filtros, rangos de fechas y token de acceso:

| Archivo | Destino | Variable `baseUrl` |
|---|---|---|
| `postman/G3Application.postman_collection.json` | Desarrollo local | `http://localhost:8080` |
| `postman/G3Application_Render.postman_collection.json` | Render | URL pública del servicio desplegado |

Cada colección comienza por `Autenticación > Iniciar sesión`: guarda automáticamente el JWT de la
respuesta en la variable `accessToken`, utilizada como Bearer token por las operaciones protegidas.
Antes de ejecutar una colección contra Render, se debe ajustar `baseUrl` a la URL efectiva del
servicio y definir las credenciales de un usuario habilitado.

## Contratos de respuesta

Las respuestas correctas incluyen los siguientes campos:

| Campo | Tipo | Descripción |
|---|---|---|
| `data` | Objeto, lista o página | Resultado del recurso u operación solicitada. |
| `message` | Texto | Descripción legible del resultado. |
| `timestamp` | Fecha y hora ISO-8601 | Momento en que se construye la respuesta. |

Una respuesta paginada contiene, dentro de `data`, el contenido solicitado y su metadato de
paginación: número de página, tamaño, total de elementos y total de páginas.

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
| `404` | Recurso solicitado no encontrado. |
| `405` | Método HTTP no admitido; se conserva la cabecera `Allow`. |
| `406` | Representación no disponible para la cabecera `Accept` solicitada. |
| `409` | Conflicto con un valor que debe ser único o transición de estado no permitida. |
| `415` | Tipo de contenido no compatible. |
| `500` | Error interno no previsto. |

## Flujo de mantenimiento preventivo

```text
Máquina ──────────────────────────────── existe por sí sola

Plantilla preventiva ─────────────────── existe por sí sola
        │
        │ se asigna a una máquina
        ▼
Asignación preventiva
├── máquina + plantilla                  pareja única
├── intervalo, aviso y próxima fecha     planificación específica del activo
└── activa / inactiva                    permite o detiene nuevas generaciones
        │
        │ proceso programado
        ▼
Preventivo
├── fecha prevista e instantánea del plan
├── PENDING                              pendiente de asignar
├── ASSIGNED                             administrador asigna un inspector
├── IN_PROGRESS                          inspector inicia y registra el trabajo
└── COMPLETED                            inspector completa con trabajo registrado
```

1. Se registra una máquina como activo mantenible. Puede existir sin ningún mantenimiento
   configurado.
2. Se crean plantillas preventivas con las acciones que deben realizarse. Una plantilla tampoco
   necesita estar asignada para existir.
3. Un administrador crea una asignación para unir una plantilla y una máquina. En ella define el
   intervalo, el margen de aviso, la próxima fecha y si debe generar nuevos mantenimientos.
4. El proceso programado consulta asignaciones activas. Cuando
   `nextDueDate ≤ fecha actual + noticeDays`, genera un preventivo pendiente y avanza la próxima
   fecha de esa asignación según su intervalo.
5. El preventivo conserva la asignación y una copia del contenido de la plantilla. Por tanto, un
   cambio posterior de la plantilla no altera las instrucciones históricas de una ejecución ya
   creada.
6. Un administrador asigna el preventivo pendiente a un usuario con rol de inspector. El
   inspector inicia la ejecución, registra o corrige el trabajo realizado y la completa.
7. Desactivar una asignación impide nuevas generaciones, pero preserva la planificación y todos
   los preventivos ya creados. Desactivar una plantilla impide emplearla en nuevas asignaciones.
