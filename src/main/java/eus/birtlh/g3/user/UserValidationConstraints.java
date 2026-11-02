package eus.birtlh.g3.user;

/**
 * Límites máximos compartidos para los campos persistentes de {@link User}.
 *
 * <p>Los DTOs deben reutilizarlos para validar la entrada y {@code User} los emplea para definir
 * las longitudes de sus columnas, manteniendo ambos límites sincronizados.</p>
 */
public final class UserValidationConstraints {

    /** Longitud máxima permitida para el nombre. */
    public static final int MAX_NAME_LENGTH = 100;

    /** Longitud máxima permitida para el apellido. */
    public static final int MAX_LAST_NAME_LENGTH = 100;

    /** Longitud máxima permitida para la dirección de correo electrónico. */
    public static final int MAX_EMAIL_LENGTH = 254;

    /** Longitud máxima permitida para el hash de contraseña almacenado. */
    public static final int MAX_PASSWORD_HASH_LENGTH = 255;

    /**
     * Impide la instanciación de esta clase de constantes.
     */
    private UserValidationConstraints() {
    }
}
