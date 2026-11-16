package eus.birtlh.g3.preventive.exceptions.plan;

/**
 * Indica que se intenta usar una plantilla preventiva inactiva para crear una asignación.
 */
public class PreventivePlanInactiveException extends RuntimeException {

    /**
     * Crea la excepción con el detalle de la plantilla no disponible.
     *
     * @param message descripción del motivo por el que la plantilla no está disponible
     */
    public PreventivePlanInactiveException(String message) {
        super(message);
    }
}
