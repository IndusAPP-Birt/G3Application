package eus.birtlh.g3.preventive.exceptions;

/**
 * Indica que no existe el plan preventivo solicitado.
 */
public class PreventivePlanNotFoundException extends RuntimeException {

    /**
     * Crea la excepción con el detalle del plan preventivo no encontrado.
     *
     * @param message detalle del error
     */
    public PreventivePlanNotFoundException(String message) {
        super(message);
    }
}
