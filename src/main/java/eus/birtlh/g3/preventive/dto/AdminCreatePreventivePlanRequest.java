package eus.birtlh.g3.preventive.dto;

import jakarta.validation.constraints.*;

/**
 * Datos necesarios para que un administrador cree una plantilla de plan preventivo.
 *
 * <p>La plantilla no se asocia a ningún activo durante su creación. Antes de validarse, elimina
 * los espacios extremos del nombre y de la descripción.</p>
 *
 * @param name nombre descriptivo de la plantilla
 * @param description acciones preventivas o instrucciones; puede ser {@code null}
 */
public record AdminCreatePreventivePlanRequest(
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
        String description
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
