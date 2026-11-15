package eus.birtlh.g3.preventive.dto.execution;

import eus.birtlh.g3.preventive.PreventiveValidationConstraints;
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
                max = PreventiveValidationConstraints.MAX_WORK_PERFORMED_LENGTH,
                message = "El trabajo realizado no puede superar los {max} caracteres."
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
