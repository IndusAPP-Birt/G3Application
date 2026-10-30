package eus.birtlh.g3.user.dto;

import eus.birtlh.g3.user.UserValidationConstraints;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO que recibe la nueva contraseña para restablecer la de un usuario.
 *
 * @param password nueva contraseña sin cifrar del usuario
 */
public record ResetUserPasswordRequest(
        @NotBlank(message = "La contraseña es obligatoria.")
        @Size(
                min = UserValidationConstraints.MIN_PASSWORD_LENGTH,
                message = "La contraseña debe tener al menos {min} caracteres."
        )
        String password
) {
}
