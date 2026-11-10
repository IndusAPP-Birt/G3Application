package eus.birtlh.g3.preventive.dto;

import jakarta.validation.constraints.*;

/**
 * Datos necesarios para que un administrador actualice completamente un plan preventivo.
 *
 * <p>No incluye la máquina a la que pertenece el plan, porque esa relación es inmutable. Antes
 * de validarse, elimina los espacios extremos del nombre y la descripción.</p>
 *
 * @param name nombre descriptivo del plan
 * @param description instrucciones o descripción del plan; puede ser {@code null}
 * @param intervalDays número de días entre preventivos consecutivos
 * @param noticeDays días de antelación para generar el preventivo; puede ser cero
 * @param active estado de activación que el administrador asigna al plan
 */
public record AdminUpdatePreventivePlanRequest(
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

        @NotNull(message = "El estado es obligatorio.")
        Boolean active
) {
    public AdminUpdatePreventivePlanRequest {
        if (name != null) {
            name = name.strip();
        }

        if (description != null) {
            description = description.strip();
        }
    }
}
