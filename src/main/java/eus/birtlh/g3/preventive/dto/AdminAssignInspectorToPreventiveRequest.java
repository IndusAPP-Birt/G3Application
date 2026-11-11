package eus.birtlh.g3.preventive.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Datos necesarios para asignar un inspector a un preventivo.
 *
 * @param inspectorId identificador del usuario inspector que recibirá la asignación
 */
public record AdminAssignInspectorToPreventiveRequest(
        @NotNull(message = "El inspector es obligatorio.")
        @Positive(message = "El identificador debe ser mayor que cero.")
        Long inspectorId
) {
}
