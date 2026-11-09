package eus.birtlh.g3.preventive.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

/**
 * Datos necesarios para que un administrador cree un plan preventivo.
 *
 * <p>Antes de validarse, elimina los espacios extremos del nombre y la descripción. El plan se
 * crea activo; su estado se administra posteriormente mediante la actualización del recurso.</p>
 *
 * @param machineId identificador de la máquina a la que pertenece el plan
 * @param name nombre descriptivo del plan
 * @param description instrucciones o descripción del plan; puede ser {@code null}
 * @param intervalDays número de días entre preventivos consecutivos
 * @param noticeDays días de antelación para generar el preventivo; puede ser cero
 * @param nextDueDate próxima fecha prevista de ejecución
 */
public record AdminCreatePreventivePlanRequest(
        @NotNull(message = "La máquina es obligatoria.")
        @Positive(message = "El identificador debe ser mayor que cero.")
        Long machineId,

        @NotBlank(message = "El nombre es obligatorio.")
        @Size(
                min = 2,
                max = 150,
                message = "El nombre debe tener entre 2 y 150 caracteres."
        )
        String name,

        @Size(
                max = 1_000,
                message = "La descripción no puede superar los 1000 caracteres."
        )
        String description,


        @Positive(message = "El intervalo debe ser mayor que cero.")
        int intervalDays,

        @PositiveOrZero(message = "El margen de aviso debe ser cero o mayor.")
        int noticeDays,

        @NotNull(message = "La próxima fecha prevista es obligatoria.")
        LocalDate nextDueDate
) {
    public AdminCreatePreventivePlanRequest {
        if (name != null) {
            name = name.strip();
        }

        if (description != null) {
            description = description.strip();
        }
    }
}
