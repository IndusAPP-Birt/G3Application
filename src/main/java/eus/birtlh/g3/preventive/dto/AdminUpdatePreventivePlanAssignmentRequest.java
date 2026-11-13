package eus.birtlh.g3.preventive.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;

/**
 * Datos necesarios para que un administrador actualice una asignación preventiva.
 *
 * <p>La plantilla preventiva y la máquina son inmutables; la actualización solo modifica el
 * calendario de generación y el estado de activación de la asignación.</p>
 *
 * @param intervalDays número de días entre mantenimientos preventivos consecutivos
 * @param noticeDays días de antelación para generar el mantenimiento preventivo
 * @param nextDueDate próxima fecha prevista de generación
 * @param active estado de activación que el administrador asigna a la planificación
 */
public record AdminUpdatePreventivePlanAssignmentRequest(
        @Positive(message = "El intervalo debe ser mayor que cero.")
        int intervalDays,

        @PositiveOrZero(message = "El margen de aviso debe ser cero o mayor.")
        int noticeDays,

        @NotNull(message = "La próxima fecha prevista es obligatoria.")
        LocalDate nextDueDate,

        @NotNull(message = "El estado es obligatorio.")
        Boolean active
) {
}
