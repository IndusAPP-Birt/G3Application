package eus.birtlh.g3.incident.exception;

/**
 * Indica que el usuario autenticado no puede realizar la operación solicitada sobre una
 * incidencia.
 */
public class IncidentAccessDeniedException extends RuntimeException {

    /**
     * Crea la excepción con el motivo por el que se deniega el acceso.
     *
     * @param message mensaje descriptivo del error
     */
    public IncidentAccessDeniedException(String message) {
        super(message);
    }
}
