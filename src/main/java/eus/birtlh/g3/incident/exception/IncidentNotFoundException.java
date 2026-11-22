package eus.birtlh.g3.incident.exception;

/**
 * Indica que no existe una incidencia con el identificador solicitado.
 */
public class IncidentNotFoundException extends RuntimeException {

    /**
     * Crea la excepción con el mensaje que describe la incidencia no encontrada.
     *
     * @param message mensaje descriptivo del error
     */
    public IncidentNotFoundException(String message) {
        super(message);
    }
}
