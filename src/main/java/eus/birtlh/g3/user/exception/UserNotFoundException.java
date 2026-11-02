package eus.birtlh.g3.user.exception;

/**
 * Indica que no existe el usuario solicitado.
 */
public class UserNotFoundException extends RuntimeException {

    /**
     * Crea la excepción con el detalle del usuario no encontrado.
     *
     * @param message detalle del error
     */
    public UserNotFoundException(String message) {
        super(message);
    }
}
