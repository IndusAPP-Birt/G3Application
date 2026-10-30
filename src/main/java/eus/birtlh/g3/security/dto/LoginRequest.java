package eus.birtlh.g3.security.dto;

import eus.birtlh.g3.user.UserValidationConstraints;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Locale;

/**
 * DTO que recibe las credenciales para iniciar sesión.
 *
 * <p>La dirección de correo electrónico se elimina de espacios en los extremos
 * y se convierte a minúsculas.</p>
 *
 * @param email dirección de correo electrónico del usuario
 * @param password contraseña sin cifrar del usuario
 */
public record LoginRequest(
        @NotBlank(message = "La dirección de correo electrónico es obligatoria.")
        @Size(
                max = UserValidationConstraints.MAX_EMAIL_LENGTH,
                message = "La dirección de correo electrónico no puede superar los {max} caracteres.")
        @Pattern(
                regexp = UserValidationConstraints.EMAIL_REGEX,
                message = "La dirección de correo electrónico debe tener un formato válido."
        )
        String email,

        @NotBlank(message = "La contraseña es obligatoria.")
        String password
) {
    public LoginRequest {
        if (email != null) {
            email = email.strip().toLowerCase(Locale.ROOT);
        }
    }
}
