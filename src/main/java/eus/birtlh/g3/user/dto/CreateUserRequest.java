package eus.birtlh.g3.user.dto;

import eus.birtlh.g3.user.Role;
import eus.birtlh.g3.user.UserValidationConstraints;
import jakarta.validation.constraints.*;

import java.util.Locale;

/**
 * DTO que recibe los datos necesarios para crear un usuario.
 *
 * <p>El nombre, los apellidos y el correo electrónico se eliminan de espacios en
 * los extremos. El correo electrónico también se convierte a minúsculas.</p>
 *
 * @param name nombre del usuario
 * @param lastName apellidos del usuario; pueden ser {@code null}
 * @param email dirección de correo electrónico del usuario
 * @param password contraseña sin cifrar del usuario
 * @param role rol asignado al usuario
 */
public record CreateUserRequest(
        @NotBlank(message = "El nombre es obligatorio.")
        @Size(
                min = UserValidationConstraints.MIN_NAME_LENGTH,
                max = UserValidationConstraints.MAX_NAME_LENGTH,
                message = "El nombre debe tener entre 2 y 100 caracteres.")
        String name,

        @Size(
                min = UserValidationConstraints.MIN_NAME_LENGTH,
                max = UserValidationConstraints.MAX_NAME_LENGTH,
                message = "Los apellidos, si se indican, deben tener entre 2 y 100 caracteres.")
        String lastName,

        @NotBlank(message = "La dirección de correo electrónico es obligatoria.")
        @Size(
                max = UserValidationConstraints.MAX_EMAIL_LENGTH,
                message = "La dirección de correo electrónico no puede superar los 254 caracteres.")
        @Pattern(
                regexp = UserValidationConstraints.EMAIL_REGEX,
                message = "La dirección de correo electrónico debe tener un formato válido."
        )
        String email,

        @NotBlank(message = "La contraseña es obligatoria.")
        @Size(
                min = UserValidationConstraints.MIN_PASSWORD_LENGTH,
                message = "La contraseña debe tener al menos 8 caracteres."
        )
        String password,

        @NotNull(message = "El rol es obligatorio.")
        Role role
) {
    public CreateUserRequest {
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
