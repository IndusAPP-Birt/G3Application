package eus.birtlh.g3.machine.mapper;

import eus.birtlh.g3.machine.Machine;
import eus.birtlh.g3.machine.dto.MachineResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Convierte entidades {@link Machine} persistidas en sus DTOs públicos de respuesta.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MachineMapper {

    /**
     * Transforma una máquina persistida en su representación pública.
     *
     * <p>El estado se expone como una opción de catálogo con un código estable y una descripción
     * legible.</p>
     *
     * @param machine máquina que se desea exponer
     * @return DTO público correspondiente a la máquina
     * @throws IllegalArgumentException si la máquina es {@code null}, no está persistida o no
     *                                  tiene estado
     */
    public static MachineResponse toResponse(Machine machine) {
        if (machine == null) {
            throw new IllegalArgumentException("La máquina es obligatoria.");
        }

        if (machine.getId() == null) {
            throw new IllegalArgumentException("La máquina debe estar persistida.");
        }

        return new MachineResponse(
                machine.getId(),
                machine.getAssetCode(),
                machine.getName(),
                machine.getManufacturer(),
                machine.getModel(),
                machine.getSerialNumber(),
                machine.getLocation(),
                machine.getCommissionedAt(),
                MachineStatusMapper.toResponse(machine.getStatus()),
                machine.getCreatedAt(),
                machine.getUpdatedAt()
        );
    }
}
