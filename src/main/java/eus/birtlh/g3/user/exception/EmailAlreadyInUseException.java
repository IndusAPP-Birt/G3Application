package eus.birtlh.g3.user.exception;

/**
 * Excepción lanzada cuando una dirección de correo electrónico ya está asignada a otro usuario.
 */
public class EmailAlreadyInUseException extends RuntimeException {

    /**
     * Crea una excepción con el mensaje de dirección de correo electrónico ya en uso.
     */
    public EmailAlreadyInUseException() {
        super("La dirección de correo electrónico ya está en uso.");
    }
}
