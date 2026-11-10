# G3 Application

API REST para la administración de usuarios, maquinaria industrial y planes de mantenimiento
preventivo. La API emplea recursos JSON, validación de entrada, paginación homogénea y respuestas
con un contrato común.

## Convenciones de la API

- Los identificadores de recursos son positivos.
- Los listados paginados aceptan `page` y `size`. La página se numera desde cero; el tamaño por
  defecto es `20` y el máximo es `100`.
- Los resultados paginados se ordenan ascendentemente por identificador para ofrecer páginas
  estables.
- Los filtros se exponen mediante rutas específicas y no se combinan entre sí.

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

Un plan preventivo pertenece de forma permanente a una máquina y define una recurrencia de
mantenimiento. Contiene nombre, descripción opcional, intervalo entre ejecuciones, margen de
aviso, próxima fecha prevista, estado de activación y marcas temporales.

La máquina asociada no puede cambiarse tras crear el plan. La próxima fecha prevista forma parte
de la planificación y se gestiona de forma independiente de la actualización administrativa del
plan.

### Endpoints

| Método | Ruta | Operación |
|---|---|---|
| `GET` | `/api/preventive-plans` | Obtiene planes preventivos paginados. |
| `GET` | `/api/preventive-plans/{id}` | Obtiene un plan preventivo por identificador. |
| `GET` | `/api/preventive-plans/by-machine/{machineId}` | Filtra planes por máquina. |
| `GET` | `/api/preventive-plans/by-status` | Filtra planes por estado de activación. |
| `GET` | `/api/preventive-plans/by-name` | Filtra por coincidencia parcial de nombre. |
| `GET` | `/api/preventive-plans/by-next-due-date` | Filtra por un rango inclusivo de próxima fecha prevista. |
| `POST` | `/api/preventive-plans` | Crea un plan preventivo activo. |
| `PUT` | `/api/preventive-plans/{id}` | Actualiza la configuración y el estado de activación. |

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
| `400` | Validación de solicitudes, parámetros ausentes o incompatibles, formato inválido o rango de fechas no válido. |
| `404` | Recurso solicitado no encontrado. |
| `405` | Método HTTP no admitido; se conserva la cabecera `Allow`. |
| `406` | Representación no disponible para la cabecera `Accept` solicitada. |
| `409` | Conflicto con un valor que debe ser único. |
| `415` | Tipo de contenido no compatible. |
| `500` | Error interno no previsto. |

## Flujo deseado de mantenimiento preventivo

```text
Máquina
  └── Plan preventivo (uno o varios por máquina)
        ├── Configuración: intervalo, aviso, próxima fecha y activación
        └── Generación automática de preventivos
              └── Preventivo: ejecución concreta para una fecha prevista
                    ├── Pendiente de asignación
                    ├── Administrador asigna un inspector
                    ├── Inspector inicia y registra el trabajo realizado
                    └── Inspector completa la ejecución
```

1. Se registra una máquina como activo mantenible.
2. Se asocian uno o varios planes preventivos a esa máquina. Cada plan representa una necesidad
   de mantenimiento independiente y define su propia frecuencia, margen de aviso y calendario.
3. Un proceso de planificación revisa los planes activos. Cuando una próxima fecha entra en su
   ventana de aviso, genera un preventivo para esa fecha y avanza la siguiente fecha del plan.
4. El preventivo conserva de forma inmutable el plan de origen y la fecha prevista, creando una
   trazabilidad estable entre la configuración y su ejecución.
5. Un administrador asigna cada preventivo pendiente a un usuario con rol de inspector. La
   ejecución pasa entonces por un ciclo operativo: pendiente, asignada, iniciada y completada.
   El cierre debe registrar el trabajo realizado antes de marcarla como completada.
6. Desactivar un plan impide generar nuevas ejecuciones, pero preserva tanto el plan como el
   histórico de preventivos ya creados.
