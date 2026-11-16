package eus.birtlh.g3.machine.exception;

/**
 * Indica que se intenta usar una máquina no activa en una operación que requiere un activo
 * operativo.
 */
public class MachineNotActiveException extends RuntimeException {

    /**
     * Crea la excepción con el detalle del estado no válido de la máquina.
     *
     * @param message descripción del motivo por el que la máquina no está disponible
     */
    public MachineNotActiveException(String message) {
        super(message);
    }
}
