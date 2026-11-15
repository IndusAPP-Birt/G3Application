package eus.birtlh.g3.preventive.exceptions.assignment;

/**
 * Indica que un inspector autenticado intenta operar un preventivo que no tiene asignado.
 */
public class InspectorNotAssignedToPreventiveException extends RuntimeException {

    /**
     * Crea la excepción con el detalle de la asignación no autorizada.
     *
     * @param message descripción del motivo por el que se deniega la operación
     */
    public InspectorNotAssignedToPreventiveException(String message) {
        super(message);
    }
}
