package eus.birtlh.g3.preventive.dto;

import eus.birtlh.g3.preventive.PreventiveStatus;
import eus.birtlh.g3.user.dto.UserResponse;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Datos públicos de una ejecución preventiva persistida, expuestos por la API.
 *
 * <p>Incluye el plan de origen y el inspector asignado mediante sus representaciones públicas.
 * El inspector puede ser {@code null} mientras el preventivo esté pendiente de asignación.</p>
 *
 * @param id identificador único del preventivo
 * @param preventivePlan plan preventivo del que procede la ejecución
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
        PreventivePlanResponse preventivePlan,
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
