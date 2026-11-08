package eus.birtlh.g3.machine.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Locale;

/**
 * Datos necesarios para que un administrador corrija el código patrimonial de una máquina.
 *
 * <p>El código se elimina de espacios externos y se normaliza a mayúsculas con
 * {@link Locale#ROOT}, manteniendo el criterio aplicado al registro de máquinas.</p>
 *
 * @param assetCode nuevo código patrimonial, obligatorio y de entre 2 y 50 caracteres
 */
public record AdminUpdateAssetCodeRequest(
        @NotBlank(message = "El código patrimonial es obligatorio.")
        @Size(
                min = 2,
                max = 50,
                message = "El código patrimonial debe tener entre 2 y 50 caracteres."
        )
        String assetCode
) {
    public AdminUpdateAssetCodeRequest {
        if (assetCode != null) {
            assetCode = assetCode.strip().toUpperCase(Locale.ROOT);
        }
    }
}
