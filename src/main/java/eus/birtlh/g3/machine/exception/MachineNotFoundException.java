package eus.birtlh.g3.machine.exception;

/**
 * Indica que no existe la máquina solicitada.
 */
public class MachineNotFoundException extends RuntimeException {

    /**
     * Crea la excepción con el detalle de la máquina no encontrada.
     *
     * @param message detalle del error
     */
    public MachineNotFoundException(String message) {
        super(message);
    }
}
