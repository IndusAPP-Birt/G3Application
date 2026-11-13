package eus.birtlh.g3.preventive.exceptions;

/**
 * Indica que se intenta completar un preventivo sin haber registrado el trabajo realizado.
 */
public class PreventiveWorkReportRequiredException extends RuntimeException {

    /**
     * Crea la excepción con el detalle del informe de trabajo requerido.
     *
     * @param message detalle del error
     */
    public PreventiveWorkReportRequiredException(String message) {
        super(message);
    }
}
