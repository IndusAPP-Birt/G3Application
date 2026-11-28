package eus.birtlh.g3.incident.dto;

import eus.birtlh.g3.incident.IncidentSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Datos necesarios para que el inspector comunique una incidencia detectada durante la ejecución
 * de un preventivo.
 *
 * <p>Antes de aplicar las restricciones de validación, elimina los espacios iniciales y finales
 * de la descripción.</p>
 *
 * @param description descripción del problema detectado
 * @param severity nivel de gravedad asignado por el inspector
 */
public record CreateIncidentRequest(
        @NotBlank(message = "La descripción de la incidencia es obligatoria.")
        @Size(
                max = 2_000,
                message = "La descripción de la incidencia no puede superar los {max} caracteres."
        )
        String description,

        @NotNull(message = "La gravedad de la incidencia es obligatoria.")
        IncidentSeverity severity
) {

    /**
     * Normaliza la descripción antes de validarla.
     */
    public CreateIncidentRequest {
        if (description != null) {
            description = description.strip();
        }
    }
}
