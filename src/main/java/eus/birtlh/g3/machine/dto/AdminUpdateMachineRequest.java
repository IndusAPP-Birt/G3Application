package eus.birtlh.g3.machine.dto;

import eus.birtlh.g3.machine.Machine;
import eus.birtlh.g3.machine.MachineStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Datos necesarios para que un administrador actualice completamente una {@link Machine}.
 *
 * <p>No incluye el código patrimonial ni la fecha de puesta en servicio, ya que son inmutables.
 * Antes de validarse, elimina los espacios extremos de todos los textos y conserva las
 * mayúsculas y minúsculas introducidas por el cliente.</p>
 *
 * @param name nombre descriptivo de la máquina
 * @param manufacturer fabricante de la máquina; puede ser {@code null}
 * @param model modelo de la máquina; puede ser {@code null}
 * @param serialNumber número de serie de la máquina; puede ser {@code null}
 * @param location ubicación de la máquina; puede ser {@code null}
 * @param status estado operativo que el administrador asigna a la máquina
 */
public record AdminUpdateMachineRequest(
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

        @NotNull(message = "El estado es obligatorio.")
        MachineStatus status
) {
    public AdminUpdateMachineRequest {
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
