package eus.birtlh.g3.preventive.dto;

import eus.birtlh.g3.preventive.PreventiveStatus;
import eus.birtlh.g3.user.dto.UserResponse;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Datos públicos de una ejecución preventiva persistida, expuestos por la API.
 *
 * <p>Incluye la asignación de plantilla y máquina de la que procede, junto con una instantánea
 * histórica de los datos de la plantilla. El inspector puede ser {@code null} mientras el
 * preventivo esté pendiente de asignación.</p>
 *
 * @param id identificador único del preventivo
 * @param preventivePlanAssignment asignación de plantilla y máquina que origina la ejecución
 * @param planName nombre histórico de la plantilla en el momento de generar el preventivo
 * @param planDescription descripción histórica de la plantilla en el momento de generar el
 *                        preventivo
 * @param assignedInspector inspector responsable; puede ser {@code null}
 * @param dueDate fecha prevista para realizar el preventivo
 * @param status estado actual de la ejecución
 * @param workPerformed trabajo realizado; puede ser {@code null} antes de registrarlo
 * @param assignedAt fecha de asignación del inspector; puede ser {@code null}
 * @param startedAt fecha de inicio de la ejecución; puede ser {@code null}
 * @param completedAt fecha de finalización de la ejecución; puede ser {@code null}
 * @param createdAt fecha de creación del preventivo
 * @param updatedAt fecha de última actualización del preventivo
 */
public record PreventiveResponse(
        long id,
        PreventivePlanAssignmentResponse preventivePlanAssignment,
        String planName,
        String planDescription,
        UserResponse assignedInspector,
        LocalDate dueDate,
        PreventiveStatus status,
        String workPerformed,
        Instant assignedAt,
        Instant startedAt,
        Instant completedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
