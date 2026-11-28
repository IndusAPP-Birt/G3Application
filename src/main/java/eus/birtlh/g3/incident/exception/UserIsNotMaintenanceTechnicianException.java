package eus.birtlh.g3.incident.exception;

/**
 * Indica que el usuario seleccionado no puede recibir una asignación de incidencia como técnico
 * de mantenimiento.
 */
public class UserIsNotMaintenanceTechnicianException extends RuntimeException {

    /**
     * Crea la excepción con el motivo por el que el usuario no puede ser asignado.
     *
     * @param message mensaje descriptivo del error
     */
    public UserIsNotMaintenanceTechnicianException(String message) {
        super(message);
    }
}
