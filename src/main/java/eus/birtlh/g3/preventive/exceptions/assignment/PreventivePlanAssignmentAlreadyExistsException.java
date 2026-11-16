package eus.birtlh.g3.preventive.exceptions.assignment;

/**
 * Indica que se intenta crear una segunda asignación para la misma pareja de plantilla
 * preventiva y máquina.
 */
public class PreventivePlanAssignmentAlreadyExistsException extends RuntimeException {

    /**
     * Crea la excepción con el detalle de la pareja duplicada.
     *
     * @param message descripción del motivo por el que no puede crearse la asignación
     */
    public PreventivePlanAssignmentAlreadyExistsException(String message) {
        super(message);
    }
}
