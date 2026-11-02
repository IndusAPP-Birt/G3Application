package eus.birtlh.g3.user.dto;

import eus.birtlh.g3.user.UserValidationConstraints;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Datos necesarios para que un administrador restablezca la contraseña de un usuario.
 *
 * <p>La contraseña se conserva sin transformaciones para que el servicio la codifique exactamente
 * como la ha proporcionado el administrador.</p>
 *
 * @param password nueva contraseña en texto plano que el servicio debe convertir en hash
 */
public record AdminResetUserPasswordRequest(
        @NotBlank(message = "La contraseña es obligatoria.")
        @Size(
                min = UserValidationConstraints.MIN_PASSWORD_LENGTH,
                max = UserValidationConstraints.MAX_PASSWORD_LENGTH,
                message = "La contraseña debe tener entre {min} y {max} caracteres."
        )
        String password
) {
}
