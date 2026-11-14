package eus.birtlh.g3.auth.dto;

import eus.birtlh.g3.user.UserValidationConstraints;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Locale;

/**
 * Credenciales necesarias para iniciar sesión.
 *
 * <p>El correo electrónico elimina los espacios extremos y se normaliza íntegramente a minúsculas
 * antes de validarse. La contraseña se conserva sin transformar para comprobarla exactamente con
 * el hash almacenado.</p>
 *
 * @param email dirección de correo electrónico normalizada del usuario
 * @param password contraseña en texto plano introducida por el usuario
 */
public record LoginRequest(
        @NotBlank(message = "La dirección de correo electrónico es obligatoria.")
        @Size(
                max = UserValidationConstraints.MAX_EMAIL_LENGTH,
                message = "La dirección de correo electrónico no puede superar los {max} caracteres.")
        @Email(message = "La dirección de correo electrónico debe tener un formato válido.")
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
