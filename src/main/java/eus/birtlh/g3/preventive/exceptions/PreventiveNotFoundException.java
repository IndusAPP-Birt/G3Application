package eus.birtlh.g3.preventive.exceptions;

/**
 * Indica que no existe el preventivo solicitado.
 */
public class PreventiveNotFoundException extends RuntimeException {

    /**
     * Crea la excepción con el detalle del preventivo no encontrado.
     *
     * @param message detalle del error
     */
    public PreventiveNotFoundException(String message) {
        super(message);
    }
}
