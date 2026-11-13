package eus.birtlh.g3.preventive.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;

/**
 * Datos necesarios para que un administrador asigne una plantilla preventiva a una máquina.
 *
 * <p>La asignación se crea activa. El plan, la máquina y su calendario inicial quedan definidos
 * en esta solicitud; las relaciones con el plan y el activo no pueden modificarse posteriormente.</p>
 *
 * @param preventivePlanId identificador de la plantilla preventiva que se asigna
 * @param machineId identificador de la máquina a la que se aplica la plantilla
 * @param intervalDays número de días entre mantenimientos preventivos consecutivos
 * @param noticeDays días de antelación para generar el mantenimiento preventivo
 * @param nextDueDate próxima fecha prevista de generación
 */
public record AdminCreatePreventivePlanAssignmentRequest(
        @NotNull(message = "El plan preventivo es obligatorio.")
        @Positive(message = "El identificador debe ser mayor que cero.")
        Long preventivePlanId,

        @NotNull(message = "La máquina es obligatoria.")
        @Positive(message = "El identificador debe ser mayor que cero.")
        Long machineId,

        @Positive(message = "El intervalo debe ser mayor que cero.")
        int intervalDays,

        @PositiveOrZero(message = "El margen de aviso debe ser cero o mayor.")
        int noticeDays,

        @NotNull(message = "La próxima fecha prevista es obligatoria.")
        LocalDate nextDueDate
        ) {
}
