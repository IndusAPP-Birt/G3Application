package eus.birtlh.g3.user;

/**
 * Límites de validación compartidos para los datos de un {@link User}.
 *
 * <p>Los DTOs los reutilizan para validar la entrada y {@code User} emplea los límites aplicables
 * a sus campos persistentes para definir las longitudes de las columnas.</p>
 */
public final class UserValidationConstraints {

    /** Longitud máxima permitida para el nombre. */
    public static final int MAX_NAME_LENGTH = 100;

    /** Longitud mínima permitida para el nombre. */
    public static final int MIN_NAME_LENGTH = 2;

    /** Longitud máxima permitida para el apellido. */
    public static final int MAX_LAST_NAME_LENGTH = 100;

    /** Longitud mínima permitida para el apellido. */
    public static final int MIN_LAST_NAME_LENGTH = 2;

    /** Longitud máxima permitida para la dirección de correo electrónico. */
    public static final int MAX_EMAIL_LENGTH = 254;

    /** Longitud mínima permitida para la contraseña en texto plano. */
    public static final int MIN_PASSWORD_LENGTH = 8;

    /** Longitud máxima permitida para la contraseña en texto plano. */
    public static final int MAX_PASSWORD_LENGTH = 64;

    /** Longitud máxima permitida para el hash de contraseña almacenado. */
    public static final int MAX_PASSWORD_HASH_LENGTH = 255;

    /**
     * Impide la instanciación de esta clase de constantes.
     */
    private UserValidationConstraints() {
    }
}
