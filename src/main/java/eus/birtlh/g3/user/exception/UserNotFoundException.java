package eus.birtlh.g3.user.exception;

/**
 * Excepción lanzada cuando no se encuentra el usuario solicitado.
 */
public class UserNotFoundException extends RuntimeException {

    /**
     * Crea una excepción con el mensaje de usuario no encontrado.
     */
    public UserNotFoundException() {
        super("Usuario no encontrado.");
    }
}
