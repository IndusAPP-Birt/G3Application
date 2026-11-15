package eus.birtlh.g3.machine;

/**
 * Límites de validación compartidos para los datos de una {@link Machine}.
 *
 * <p>Los DTOs los reutilizan para validar la entrada y {@code Machine} emplea los límites máximos
 * aplicables a sus campos persistentes para definir las longitudes de las columnas.</p>
 */
public final class MachineValidationConstraints {

    /** Longitud máxima permitida para el código patrimonial. */
    public static final int MAX_ASSET_CODE_LENGTH = 50;

    /** Longitud mínima permitida para el código patrimonial. */
    public static final int MIN_ASSET_CODE_LENGTH = 2;

    /** Longitud máxima permitida para el nombre. */
    public static final int MAX_NAME_LENGTH = 100;

    /** Longitud mínima permitida para el nombre. */
    public static final int MIN_NAME_LENGTH = 2;

    /** Longitud máxima permitida para el fabricante. */
    public static final int MAX_MANUFACTURER_LENGTH = 100;

    /** Longitud mínima permitida para el fabricante. */
    public static final int MIN_MANUFACTURER_LENGTH = 2;

    /** Longitud máxima permitida para el modelo. */
    public static final int MAX_MODEL_LENGTH = 100;

    /** Longitud mínima permitida para el modelo. */
    public static final int MIN_MODEL_LENGTH = 2;

    /** Longitud máxima permitida para el número de serie. */
    public static final int MAX_SERIAL_NUMBER_LENGTH = 100;

    /** Longitud mínima permitida para el número de serie. */
    public static final int MIN_SERIAL_NUMBER_LENGTH = 2;

    /** Longitud máxima permitida para la ubicación. */
    public static final int MAX_LOCATION_LENGTH = 150;

    /** Longitud mínima permitida para la ubicación. */
    public static final int MIN_LOCATION_LENGTH = 2;

    /**
     * Impide la instanciación de esta clase de constantes.
     */
    private MachineValidationConstraints() {
    }
}
