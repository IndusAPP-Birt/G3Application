package eus.birtlh.g3.user.exception;

/**
 * Indica que el correo electrónico solicitado ya está asignado a otro usuario.
 */
public class EmailAlreadyInUseException extends RuntimeException {

    /**
     * Crea la excepción con el detalle del correo electrónico duplicado.
     *
     * @param message detalle del error
     */
    public EmailAlreadyInUseException(String message) {
        super(message);
    }
}
