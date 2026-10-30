package eus.birtlh.g3.user;

/**
 * Agrupa las reglas y límites de validación compartidos entre los DTOs y la entidad {@link User}.
 */
public final class UserValidationConstraints {

    public static final int MIN_NAME_LENGTH = 2;
    public static final int MAX_NAME_LENGTH = 100;
    public static final int MIN_LAST_NAME_LENGTH = 2;
    public static final int MAX_LAST_NAME_LENGTH = 100;
    public static final int MAX_EMAIL_LENGTH = 254;
    public static final int MIN_PASSWORD_LENGTH = 8;
    // TODO: Con BCrypt, validar un máximo de 72 bytes UTF-8 de contraseña, según la recomendación de OWASP.
    public static final int MAX_PASSWORD_HASH_LENGTH = 255;

    public static final String EMAIL_REGEX =
            "^[A-Za-z0-9_+&*-]+(?:\\.[A-Za-z0-9_+&*-]+)*@"
                    + "(?:[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?\\.)+[A-Za-z]{2,}$";

    private UserValidationConstraints() {
    }
}
