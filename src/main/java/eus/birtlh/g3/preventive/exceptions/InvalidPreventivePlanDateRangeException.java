package eus.birtlh.g3.preventive.exceptions;

/**
 * Indica que el rango de fechas solicitado para consultar planes preventivos no es válido.
 */
public class InvalidPreventivePlanDateRangeException extends RuntimeException {

    /**
     * Crea la excepción con el detalle del rango de fechas inválido.
     *
     * @param message detalle del error
     */
    public InvalidPreventivePlanDateRangeException(String message) {
        super(message);
    }
}
