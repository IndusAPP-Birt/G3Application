package eus.birtlh.g3.machine.dto;

import eus.birtlh.g3.machine.Machine;
import eus.birtlh.g3.machine.MachineStatus;
import eus.birtlh.g3.machine.MachineValidationConstraints;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Datos necesarios para que un administrador actualice completamente una {@link Machine}.
 *
 * <p>No incluye el código patrimonial, que se corrige mediante un caso de uso específico, ni la
 * fecha de puesta en servicio, que es inmutable. Antes de validarse, elimina los espacios
 * extremos de todos los textos y conserva las mayúsculas y minúsculas introducidas por el
 * cliente.</p>
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
                min = MachineValidationConstraints.MIN_NAME_LENGTH,
                max = MachineValidationConstraints.MAX_NAME_LENGTH,
                message = "El nombre debe tener entre {min} y {max} caracteres."
        )
        String name,

        @Size(
                min = MachineValidationConstraints.MIN_MANUFACTURER_LENGTH,
                max = MachineValidationConstraints.MAX_MANUFACTURER_LENGTH,
                message = "El fabricante, si se indica, debe tener entre {min} y {max} caracteres."
        )
        String manufacturer,

        @Size(
                min = MachineValidationConstraints.MIN_MODEL_LENGTH,
                max = MachineValidationConstraints.MAX_MODEL_LENGTH,
                message = "El modelo, si se indica, debe tener entre {min} y {max} caracteres."
        )
        String model,

        @Size(
                min = MachineValidationConstraints.MIN_SERIAL_NUMBER_LENGTH,
                max = MachineValidationConstraints.MAX_SERIAL_NUMBER_LENGTH,
                message = "El número de serie, si se indica, debe tener entre {min} y {max} caracteres."
        )
        String serialNumber,

        @Size(
                min = MachineValidationConstraints.MIN_LOCATION_LENGTH,
                max = MachineValidationConstraints.MAX_LOCATION_LENGTH,
                message = "La ubicación, si se indica, debe tener entre {min} y {max} caracteres."
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
