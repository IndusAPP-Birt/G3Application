# Plan de pruebas

## Objetivo

Proteger con pruebas automatizadas las reglas de negocio y autorización que sustentan el flujo de
mantenimiento preventivo. La prioridad no es maximizar un porcentaje de cobertura, sino evitar
regresiones en operaciones que podrían alterar datos o permitir accesos indebidos.

## Base técnica

- Mantener las pruebas de servicios que requieren persistencia como pruebas de integración con
  Spring Boot y H2.
- Crear un perfil de pruebas con una clave JWT Base64 ficticia y válida para no depender de una
  variable de entorno local.
- Añadir `spring-security-test` para construir solicitudes autenticadas en las pruebas HTTP.
- Usar `MockMvc` para comprobar la matriz de permisos sin levantar un servidor real.
- Mantener los datos de cada prueba aislados y no depender de identificadores de `data.sql`.

## Fase 1 · Reglas de ejecución preventiva

Crear pruebas para `PreventiveService`.

| Caso | Resultado esperado |
|---|---|
| Inspector asignado inicia un preventivo `ASSIGNED` | El preventivo pasa a `IN_PROGRESS`. |
| Inspector distinto del asignado intenta iniciar, registrar trabajo o completar | Se lanza `InspectorNotAssignedToPreventiveException`. |
| Inicio desde `PENDING` o `COMPLETED` | Se rechaza la transición. |
| Registro de trabajo fuera de `IN_PROGRESS` | Se rechaza la operación. |
| Finalización sin `workPerformed` | Se lanza `PreventiveWorkReportRequiredException`. |
| Finalización con trabajo registrado | El preventivo pasa a `COMPLETED`. |

## Fase 2 · Creación de asignaciones preventivas

Crear pruebas para `PreventivePlanAssignmentService`.

| Caso | Resultado esperado |
|---|---|
| Plantilla activa y máquina `ACTIVE` | Se crea la asignación. |
| Plantilla inactiva | Se lanza `PreventivePlanInactiveException`. |
| Máquina `INACTIVE` o `RETIRED` | Se lanza `MachineNotActiveException`. |
| Pareja plantilla–máquina ya existente | Se lanza `PreventivePlanAssignmentAlreadyExistsException`. |

La restricción única de base de datos debe mantenerse como garantía final ante solicitudes
simultáneas, aunque el servicio detecte los duplicados habituales antes de persistir.

## Fase 3 · Matriz de permisos HTTP

Crear pruebas con `MockMvc` para las rutas de mayor riesgo.

| Ruta | Sin token | Administrador | Inspector | Técnico |
|---|:---:|:---:|:---:|:---:|
| `GET /api/preventives/me` | `401` | `403` | `200` | `403` |
| `PUT /api/preventives/{id}/start` | `401` | `403` | Permitido por rol; el servicio valida propiedad | `403` |
| `POST /api/preventive-plan-assignments` | `401` | `201` o error de negocio | `403` | `403` |
| `GET /api/preventives` | `401` | `200` | `403` | `403` |

Las pruebas de permisos verifican el acceso por rol. La propiedad del preventivo concreto se
mantiene en las pruebas de servicio de la fase 1.

## Fase 4 · Regresión y automatización

- Ejecutar `./mvnw test` antes de cada integración en `main`.
- Ejecutar la misma batería en integración continua cuando se configure CI.
- Añadir un caso de regresión cada vez que se corrija un defecto de negocio o seguridad.

## Fuera de alcance inicial

- Cobertura exhaustiva de DTOs, mappers, getters o respuestas triviales.
- Pruebas de correo SMTP real; el listener debe probarse mediante mocks.
- Pruebas de incidencias correctivas, informes o reasignación en curso mientras esas capacidades
  no existan en la aplicación.
