package eus.birtlh.g3.user.dto;

import eus.birtlh.g3.user.Role;
import eus.birtlh.g3.user.UserValidationConstraints;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Locale;

/**
 * Datos necesarios para que un administrador cree un usuario.
 *
 * <p>Antes de validarse, elimina los espacios extremos de nombre, apellidos y correo electrónico.
 * Por política de la aplicación, el correo electrónico completo se normaliza a minúsculas. La
 * contraseña se mantiene sin transformar para conservar exactamente el valor introducido.</p>
 *
 * @param name nombre del usuario
 * @param lastName apellido del usuario; puede ser {@code null}
 * @param email dirección de correo electrónico normalizada
 * @param password contraseña en texto plano que el servicio debe convertir en hash
 * @param role rol que el administrador asigna al usuario
 */
public record AdminCreateUserRequest(
        @NotBlank(message = "El nombre es obligatorio.")
        @Size(
                min = UserValidationConstraints.MIN_NAME_LENGTH,
                max = UserValidationConstraints.MAX_NAME_LENGTH,
                message = "El nombre debe tener entre {min} y {max} caracteres."
        )
        String name,

        @Size(
            min = UserValidationConstraints.MIN_LAST_NAME_LENGTH,
            max = UserValidationConstraints.MAX_LAST_NAME_LENGTH,
            message = "Los apellidos, si se indican, deben tener entre {min} y {max} caracteres."
        )
        String lastName,

        @NotBlank(message = "La dirección de correo electrónico es obligatoria.")
        @Size(
                max = UserValidationConstraints.MAX_EMAIL_LENGTH,
                message = "La dirección de correo electrónico no puede superar los {max} caracteres.")
        @Email(message = "La dirección de correo electrónico debe tener un formato válido.")
        String email,

        @NotBlank(message = "La contraseña es obligatoria.")
        @Size(
                min = UserValidationConstraints.MIN_PASSWORD_LENGTH,
                max = UserValidationConstraints.MAX_PASSWORD_LENGTH,
                message = "La contraseña debe tener entre {min} y {max} caracteres."
        )
        String password,

        @NotNull(message = "El rol es obligatorio.")
        Role role
) {
    public AdminCreateUserRequest {
        if (name != null) {
            name = name.strip();
        }

        if (lastName != null) {
            lastName = lastName.strip();
        }

        if (email != null) {
            email = email.strip().toLowerCase(Locale.ROOT);
        }
    }
}
