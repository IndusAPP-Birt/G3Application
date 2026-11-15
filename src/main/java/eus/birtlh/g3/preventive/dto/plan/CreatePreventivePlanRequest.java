package eus.birtlh.g3.preventive.dto.plan;

import eus.birtlh.g3.preventive.PreventiveValidationConstraints;
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
public record CreatePreventivePlanRequest(
        @NotBlank(message = "El nombre es obligatorio.")
        @Size(
                min = PreventiveValidationConstraints.MIN_NAME_LENGTH,
                max = PreventiveValidationConstraints.MAX_NAME_LENGTH,
                message = "El nombre debe tener entre {min} y {max} caracteres."
        )
        String name,

        @Size(
                max = PreventiveValidationConstraints.MAX_DESCRIPTION_LENGTH,
                message = "La descripción no puede superar los {max} caracteres."
        )
        String description
) {
    public CreatePreventivePlanRequest {
        if (name != null) {
            name = name.strip();
        }

        if (description != null) {
            description = description.strip();
        }
    }
}
