package eus.birtlh.g3.preventive.exceptions;

/**
 * Indica que una operación no puede ejecutarse desde el estado actual de un preventivo.
 */
public class InvalidPreventiveStatusTransitionException extends RuntimeException {

    /**
     * Crea la excepción con el detalle de la transición de estado no permitida.
     *
     * @param message detalle del error
     */
    public InvalidPreventiveStatusTransitionException(String message) {
        super(message);
    }
}
