package eus.birtlh.g3.preventive.exceptions;

/**
 * Indica que el rango de fechas solicitado para consultar recursos preventivos no es válido.
 */
public class InvalidPreventiveDateRangeException extends RuntimeException {

    /**
     * Crea la excepción con el detalle del rango de fechas inválido.
     *
     * @param message detalle del error
     */
    public InvalidPreventiveDateRangeException(String message) {
        super(message);
    }
}
