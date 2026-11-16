# Auditoría de la aplicación

Fecha: 2026-11-15  
Alcance: estructura, dominio, persistencia, seguridad, validación, transacciones, generación
programada, notificaciones, pruebas, documentación y consola.

La rama `develop` estaba limpia durante la auditoría. La batería `./mvnw test -q` se ejecutó
correctamente proporcionando una clave JWT Base64 válida mediante variable de entorno temporal.
No se conservan artefactos de compilación o pruebas.

## Hallazgos y mejoras

| Prioridad | Hallazgo | Impacto | Acción propuesta |
|---|---|---|---|
| ~~P1~~ | ~~Un inspector puede iniciar, registrar trabajo o completar cualquier preventivo si conoce su identificador. La autorización HTTP comprueba el rol, pero [PreventiveService](../src/main/java/eus/birtlh/g3/preventive/PreventiveService.java) no contrasta al usuario autenticado con `assignedInspector`.~~ | ~~Un inspector podría operar un preventivo asignado a otro inspector.~~ | ~~Recibir la identidad autenticada en los casos de uso de ejecución y comprobar que coincide con el inspector asignado.~~ |
| ~~P1~~ | ~~Un inspector no puede consultar su propia carga: los `GET /api/preventives/**` pertenecen actualmente al administrador.~~ | ~~El inspector no dispone de una bandeja propia para localizar sus asignaciones.~~ | ~~Implementar el TODO `GET /api/preventives/me` y restringirlo al inspector autenticado.~~ |
| P1 | La configuración actual usa H2 en memoria, `create-drop` y `data.sql` también al desplegar. | Un reinicio del servicio elimina cambios y repuebla datos de demostración. Si Render debe conservar actividad real, no hay persistencia operativa. | Separar perfiles de desarrollo y despliegue; usar una base persistente para el segundo y reservar los datos semilla para desarrollo. |
| ~~P2~~ | ~~[JwtConfiguration](../src/main/java/eus/birtlh/g3/auth/config/JwtConfiguration.java) decodifica la clave Base64, pero no valida que tenga al menos 256 bits para HS256.~~ | ~~Una clave válida pero demasiado corta debilitaría la firma JWT.~~ | ~~Validar su longitud al arrancar y abortar con un mensaje de configuración claro cuando sea insuficiente.~~ |
| P2 | Crear de nuevo la misma pareja plantilla-máquina depende de la restricción única de base de datos. La excepción resultante se traduce en HTTP 500. | Un duplicado o reintento normal de cliente no recibe un conflicto de negocio comprensible. | Comprobar la pareja antes de crear y devolver HTTP 409 mediante una excepción específica. La restricción única debe mantenerse como garantía final. |
| P2 | Se puede crear una asignación con una plantilla inactiva, aunque el estado `active` describe su disponibilidad para nuevas asignaciones. | Incoherencia entre la regla publicada y el comportamiento del servicio. | Rechazar la creación de asignaciones cuando el plan preventivo esté inactivo. |
| P2 | Deshabilitar un usuario, cambiarle el rol o restablecer su contraseña no invalida un JWT emitido previamente. | El token conserva sus permisos anteriores hasta su caducidad, actualmente 15 minutos. | Decidir si esa ventana es aceptable. Si no lo es, incorporar una política de revocación o versionado de tokens. |
| P2 | Las pruebas dependen de que `JWT_SIGNING_KEY` esté configurada externamente. Con una clave temporal válida la batería completa pasa, pero sin ella no es reproducible de forma autónoma. | Localmente y en CI hay que preparar una variable de entorno antes de ejecutar los tests. | Añadir una configuración exclusiva de pruebas con una clave Base64 ficticia y válida; la clave real debe continuar siendo obligatoria fuera de las pruebas. |
| P2 | La cobertura actual no protege las reglas principales de negocio ni la matriz de permisos. | Cambios futuros pueden romper transiciones, propiedad del inspector o autorización sin que la batería lo detecte. | Añadir pruebas para el ciclo de estados, propiedad del inspector, duplicados de asignación, planes inactivos, JWT y permisos HTTP. |
| P3 | La generación planificada no coordina varias instancias ni define de forma explícita cómo recuperar periodos prolongados sin ejecución. | No afecta mientras exista una instancia. Al escalar, dos procesos podrían competir por la misma generación. | Mantener el diseño actual con una instancia; definir bloqueo distribuido y política de recuperación solo si se despliega con más de una. |
| P3 | Los mapeos paginados de preventivos navegan relaciones anidadas durante la conversión a DTO. La medición de `GET /api/preventives?page=0&size=20` devolvió 18 resultados y ejecutó 42 consultas SQL: una principal y 41 cargas perezosas adicionales. | El patrón N+1 está confirmado y su coste crecerá con el tamaño de cada página. | Aplicar un `@EntityGraph` para cargar la asignación, su plantilla, su máquina y el inspector junto al preventivo. Después, repetir la medición para verificar la reducción de consultas. |
| P4 | [g3-console.html](../src/main/resources/static/g3-console.html) conserva una configuración antigua de planes con rutas ya inexistentes, aunque se sobrescribe posteriormente y no llega a ejecutarse. | Deuda de mantenimiento y posible confusión al modificar la consola. | Eliminar el bloque obsoleto en una tarea específica de mantenimiento de la consola. |
| ~~P4~~ | ~~Parte de [PreventivePlanAssignmentService](../src/main/java/eus/birtlh/g3/preventive/PreventivePlanAssignmentService.java) y dos métodos de [PreventiveRepository](../src/main/java/eus/birtlh/g3/preventive/repository/PreventiveRepository.java) no tienen Javadocs, a diferencia del resto del módulo.~~ | ~~Solo afecta a la consistencia documental.~~ | ~~Completar la documentación en un commit exclusivo de `docs`.~~ |

## Aspectos verificados sin incidencia

- Separación de dominio coherente entre `PreventivePlan`, `PreventivePlanAssignment` y
  `Preventive`.
- Restricción única correcta para la pareja plantilla-máquina y para la fecha prevista dentro de
  cada asignación.
- Validaciones y normalización de entradas centralizadas mediante clases de constraints.
- Notificación de asignación publicada después de confirmar la transacción; un error SMTP no
  revierte el preventivo ya asignado.
- Errores HTTP, paginación y respuestas de API tienen contratos homogéneos.
- La seguridad deniega por defecto las rutas que no están declaradas.
- La consola, el README y las colecciones Postman recogen los endpoints implementados; los
  endpoints futuros se distinguen como TODO en el README.

## Límites de alcance

La gestión de incidencias correctivas, el estado `BLOCKED` y los informes no se evalúan como
carencias: todavía no forman parte de los recursos implementados en esta versión.
