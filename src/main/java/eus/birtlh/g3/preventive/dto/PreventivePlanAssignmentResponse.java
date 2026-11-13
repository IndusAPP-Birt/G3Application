package eus.birtlh.g3.preventive.dto;

import eus.birtlh.g3.machine.dto.MachineResponse;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Datos públicos de una asignación de plantilla preventiva a una máquina, expuestos por la API.
 *
 * <p>Incluye el contexto de la plantilla y del activo, junto con la planificación específica que
 * determina la generación de mantenimientos preventivos.</p>
 *
 * @param id identificador único de la asignación
 * @param preventivePlan plantilla preventiva asignada
 * @param machine máquina a la que se aplica la plantilla
 * @param intervalDays número de días entre mantenimientos preventivos consecutivos
 * @param noticeDays días de antelación para generar el mantenimiento preventivo
 * @param nextDueDate próxima fecha pendiente de generación
 * @param active indica si la asignación genera nuevos mantenimientos preventivos
 * @param createdAt fecha de creación de la asignación
 * @param updatedAt fecha de última actualización de la asignación
 */
public record PreventivePlanAssignmentResponse(
        long id,
        PreventivePlanResponse preventivePlan,
        MachineResponse machine,
        int intervalDays,
        int noticeDays,
        LocalDate nextDueDate,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
