package eus.birtlh.g3.preventive.exceptions;

/**
 * Indica que el usuario seleccionado para asignar un preventivo no tiene el rol de inspector.
 */
public class UserIsNotInspectorException extends RuntimeException {

    /**
     * Crea la excepción con el detalle del rol no válido para la asignación.
     *
     * @param message detalle del error
     */
    public UserIsNotInspectorException(String message) {
        super(message);
    }
}
