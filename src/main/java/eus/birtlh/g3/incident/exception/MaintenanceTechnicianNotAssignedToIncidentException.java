package eus.birtlh.g3.incident.exception;

/**
 * Indica que el técnico de mantenimiento autenticado intenta operar una incidencia que no tiene
 * asignada.
 */
public class MaintenanceTechnicianNotAssignedToIncidentException extends RuntimeException {

    /**
     * Crea la excepción con el detalle de la falta de asignación.
     *
     * @param message mensaje descriptivo del error
     */
    public MaintenanceTechnicianNotAssignedToIncidentException(String message) {
        super(message);
    }
}
