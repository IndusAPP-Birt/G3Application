package eus.birtlh.g3.machine.mapper;

import eus.birtlh.g3.common.dto.CatalogOptionResponse;
import eus.birtlh.g3.machine.MachineStatus;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Convierte los estados internos de máquina en opciones de catálogo para la API.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MachineStatusMapper {

    /**
     * Transforma un estado de máquina en su código y descripción legible.
     *
     * @param status estado que se desea exponer
     * @return opción de catálogo correspondiente al estado
     * @throws IllegalArgumentException si el estado es {@code null}
     */
    public static CatalogOptionResponse toResponse(MachineStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("El estado de la máquina es obligatorio.");
        }

        String description = switch (status) {
            case ACTIVE -> "Activa";
            case INACTIVE -> "Inactiva";
            case RETIRED -> "Retirada";
        };

        return new CatalogOptionResponse(status.name(), description);
    }
}
