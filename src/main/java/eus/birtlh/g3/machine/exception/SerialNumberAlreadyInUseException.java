package eus.birtlh.g3.machine.exception;

/**
 * Indica que el número de serie solicitado ya está asignado a otra máquina.
 */
public class SerialNumberAlreadyInUseException extends RuntimeException {

    /**
     * Crea la excepción con el detalle del número de serie duplicado.
     *
     * @param message detalle del error
     */
    public SerialNumberAlreadyInUseException(String message) {
        super(message);
    }
}
