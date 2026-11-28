package eus.birtlh.g3.incident.exception;

/**
 * Indica que la operación solicitada no está permitida desde el estado actual de una incidencia.
 */
public class InvalidIncidentStatusTransitionException extends RuntimeException {

    /**
     * Crea la excepción con el detalle de la transición de estado no permitida.
     *
     * @param message mensaje descriptivo del error
     */
    public InvalidIncidentStatusTransitionException(String message) {
        super(message);
    }
}
