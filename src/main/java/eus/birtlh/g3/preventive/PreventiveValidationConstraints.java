package eus.birtlh.g3.preventive;

/**
 * Límites de validación compartidos por las plantillas y las ejecuciones preventivas.
 *
 * <p>Los DTOs los reutilizan para validar las entradas, mientras que {@link PreventivePlan} y
 * {@link Preventive} emplean los límites máximos aplicables para definir las longitudes de sus
 * columnas persistentes.</p>
 */
public final class PreventiveValidationConstraints {

    /** Longitud mínima permitida para el nombre de la plantilla preventiva. */
    public static final int MIN_NAME_LENGTH = 2;

    /** Longitud máxima permitida para el nombre de la plantilla preventiva. */
    public static final int MAX_NAME_LENGTH = 150;

    /** Longitud máxima permitida para la descripción de la plantilla preventiva. */
    public static final int MAX_DESCRIPTION_LENGTH = 1_000;

    /** Longitud máxima permitida para el informe de trabajo de una ejecución preventiva. */
    public static final int MAX_WORK_PERFORMED_LENGTH = 2_000;

    /** Impide la instanciación de esta clase de constantes. */
    private PreventiveValidationConstraints() {
    }
}
