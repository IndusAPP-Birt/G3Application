package eus.birtlh.g3.machine.exception;

/**
 * Indica que el código patrimonial solicitado ya está asignado a otra máquina.
 */
public class AssetCodeAlreadyInUseException extends RuntimeException {

    /**
     * Crea la excepción con el detalle del código patrimonial duplicado.
     *
     * @param message detalle del error
     */
    public AssetCodeAlreadyInUseException(String message) {
        super(message);
    }
}
