# Cambios pendientes

Este documento recoge exclusivamente los cambios técnicos que permanecen pendientes y el motivo por el que todavía no se han abordado.

| Prioridad | Cambio pendiente | Motivo de aplazamiento |
|---|---|---|
| P1 | Sustituir la configuración actual de H2 en memoria, `create-drop` y `data.sql` en despliegue por una base de datos persistente. | Es una mejora importante. La parte de producción que se mostrará se migrará a Neon. Antes hay que decidir cómo se poblarán las bases de datos, preparar la estrategia de datos iniciales y realizar pruebas contra la nueva base de datos. |
| P2 | Decidir si deben revocarse de forma inmediata los JWT emitidos al deshabilitar un usuario, cambiar su rol o restablecer su contraseña. | Se acepta temporalmente la ventana actual de 15 minutos por simplicidad. El cierre de sesión corresponde al frontend, que elimina el token almacenado. Más adelante se valorará con el frontend si es posible forzar la eliminación del token cuando se deshabilite un usuario; esta medida no sustituye una revocación gestionada por el servidor. |
| P3 | Coordinar la generación preventiva si se despliega más de una instancia y definir la recuperación de periodos sin ejecución. | La aplicación no necesita todavía varias instancias ni una política de recuperación definida, pero es una mejora útil que se abordará al consolidar el despliegue o antes de escalarlo. |
| P3 | Eliminar el patrón N+1 confirmado en los listados paginados de preventivos. | El volumen actual de datos es reducido, por lo que no supone un problema operativo inmediato. Aun así, es una buena práctica pendiente: se abordará mediante una mejora específica de JPA y una nueva medición posterior. |
