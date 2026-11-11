package eus.birtlh.g3.preventive.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RecordPreventiveWorkRequest(
        @NotBlank(message = "El trabajo realizado es obligatorio.")
        @Size(
                max = 2_000,
                message = "El trabajo realizado no puede superar los 2000 caracteres."
        )
        String workPerformed
) {
    public RecordPreventiveWorkRequest {
        if (workPerformed != null) {
            workPerformed = workPerformed.strip();
        }
    }
}
