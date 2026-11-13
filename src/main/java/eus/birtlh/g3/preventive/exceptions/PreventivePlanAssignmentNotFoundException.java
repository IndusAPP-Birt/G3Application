package eus.birtlh.g3.preventive.exceptions;

/**
 * Indica que no existe una asignación de plantilla preventiva a máquina con el identificador
 * solicitado.
 */
public class PreventivePlanAssignmentNotFoundException extends RuntimeException {

    /**
     * Crea la excepción con el mensaje que se devolverá al cliente.
     *
     * @param message descripción de la asignación no encontrada
     */
    public PreventivePlanAssignmentNotFoundException(String message) {
        super(message);
    }
}
