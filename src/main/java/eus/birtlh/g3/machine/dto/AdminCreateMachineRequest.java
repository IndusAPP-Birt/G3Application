package eus.birtlh.g3.machine.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Locale;

/**
 * Datos necesarios para que un administrador registre una máquina industrial.
 *
 * <p>Antes de validarse, elimina los espacios extremos de todos los textos. Por política de la
 * aplicación, el código patrimonial se normaliza a mayúsculas; el resto de valores conserva las
 * mayúsculas y minúsculas introducidas por el cliente.</p>
 *
 * @param assetCode código patrimonial único de la máquina, normalizado a mayúsculas
 * @param name nombre descriptivo de la máquina
 * @param manufacturer fabricante de la máquina; puede ser {@code null}
 * @param model modelo de la máquina; puede ser {@code null}
 * @param serialNumber número de serie de la máquina; puede ser {@code null}
 * @param location ubicación de la máquina; puede ser {@code null}
 * @param commissionedAt fecha de puesta en servicio de la máquina
 */
public record AdminCreateMachineRequest(
        @NotBlank(message = "El código patrimonial es obligatorio.")
        @Size(
                min = 2,
                max = 50,
                message = "El código patrimonial debe tener entre 2 y 50 caracteres."
        )
        String assetCode,

        @NotBlank(message = "El nombre es obligatorio.")
        @Size(
                min = 2,
                max = 100,
                message = "El nombre debe tener entre 2 y 100 caracteres."
        )
        String name,

        @Size(
                min = 2,
                max = 100,
                message = "El fabricante, si se indica, debe tener entre 2 y 100 caracteres."
        )
        String manufacturer,

        @Size(
                min = 2,
                max = 100,
                message = "El modelo, si se indica, debe tener entre 2 y 100 caracteres."
        )
        String model,

        @Size(
                min = 2,
                max = 100,
                message = "El número de serie, si se indica, debe tener entre 2 y 100 caracteres."
        )
        String serialNumber,

        @Size(
                min = 2,
                max = 150,
                message = "La ubicación, si se indica, debe tener entre 2 y 150 caracteres."
        )
        String location,

        @NotNull(message = "La fecha de puesta en servicio es obligatoria.")
        @PastOrPresent(message = "La fecha de puesta en servicio no puede ser futura.")
        LocalDate commissionedAt
) {
    public AdminCreateMachineRequest {
        if (assetCode != null) {
            assetCode = assetCode.strip().toUpperCase(Locale.ROOT);
        }

        if (name != null) {
            name = name.strip();
        }

        if (manufacturer != null) {
            manufacturer = manufacturer.strip();
        }

        if (model != null) {
            model = model.strip();
        }

        if (serialNumber != null) {
            serialNumber = serialNumber.strip();
        }

        if (location != null) {
            location = location.strip();
        }
    }
}
