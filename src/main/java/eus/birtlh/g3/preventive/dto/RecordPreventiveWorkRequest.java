package eus.birtlh.g3.preventive.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Datos con los que un inspector registra o corrige el trabajo realizado en un preventivo en
 * curso.
 *
 * @param workPerformed descripción del trabajo realizado, obligatoria y de hasta 2.000 caracteres
 */
public record RecordPreventiveWorkRequest(
        @NotBlank(message = "El trabajo realizado es obligatorio.")
        @Size(
                max = 2_000,
                message = "El trabajo realizado no puede superar los 2000 caracteres."
        )
        String workPerformed
) {

    /**
     * Elimina los espacios iniciales y finales antes de aplicar las validaciones del contrato.
     */
    public RecordPreventiveWorkRequest {
        if (workPerformed != null) {
            workPerformed = workPerformed.strip();
        }
    }
}
