package eus.birtlh.g3.preventive.dto;

import jakarta.validation.constraints.*;

/**
 * Datos necesarios para que un administrador actualice una plantilla de plan preventivo.
 *
 * <p>Antes de validarse, elimina los espacios extremos del nombre y de la descripción. La
 * asignación de la plantilla a activos y su planificación se administran mediante recursos
 * específicos.</p>
 *
 * @param name nombre descriptivo de la plantilla
 * @param description acciones preventivas o instrucciones; puede ser {@code null}
 * @param active disponibilidad que el administrador asigna a la plantilla
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
