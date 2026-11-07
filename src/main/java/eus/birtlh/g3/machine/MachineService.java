package eus.birtlh.g3.machine;

import eus.birtlh.g3.common.dto.CatalogOptionResponse;
import eus.birtlh.g3.common.dto.PageResponse;
import eus.birtlh.g3.machine.dto.MachineResponse;
import eus.birtlh.g3.machine.exception.MachineNotFoundException;
import eus.birtlh.g3.machine.mapper.MachineMapper;
import eus.birtlh.g3.machine.mapper.MachineStatusMapper;
import eus.birtlh.g3.machine.repository.MachineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * Servicio de aplicación que coordina los casos de uso de máquinas industriales.
 *
 * <p>Obtiene entidades {@link Machine} y las transforma en DTOs públicos antes de devolverlas a
 * la capa HTTP.</p>
 */
@Service
@RequiredArgsConstructor
public class MachineService {

    private final MachineRepository machineRepository;

    /**
     * Obtiene una página de máquinas registradas como DTOs de respuesta.
     *
     * @param page índice de página basado en cero
     * @param size número de máquinas por página
     * @return página de máquinas públicas; su contenido puede estar vacío
     */
    @Transactional(readOnly = true)
    public PageResponse<MachineResponse> getAllMachines(int page, int size) {
        Page<Machine> machines = machineRepository.findAll(
                PageRequest.of(page, size, Sort.by("id").ascending())
        );

        return new PageResponse<>(
                machines.getContent()
                        .stream()
                        .map(MachineMapper::toResponse)
                        .toList(),
                machines.getNumber(),
                machines.getSize(),
                machines.getTotalElements(),
                machines.getTotalPages()
        );
    }

    /**
     * Obtiene todos los estados operativos disponibles como opciones de catálogo públicas.
     *
     * @return lista de estados disponibles con su código y descripción
     */
    @Transactional(readOnly = true)
    public List<CatalogOptionResponse> getAllStatuses() {
        return Arrays.stream(MachineStatus.values())
                .map(MachineStatusMapper::toResponse)
                .toList();
    }

    /**
     * Obtiene una máquina por su identificador como DTO de respuesta.
     *
     * @param id identificador de la máquina solicitada
     * @return DTO público de la máquina encontrada
     * @throws MachineNotFoundException si no existe una máquina con el identificador indicado
     */
    @Transactional(readOnly = true)
    public MachineResponse getMachineById(Long id) {
        Machine machine = machineRepository.findById(id)
                .orElseThrow(() -> new MachineNotFoundException(
                        "Máquina con " + id + " no encontrada."
                ));

        return MachineMapper.toResponse(machine);
    }

    /**
     * Obtiene una máquina por su código patrimonial como DTO de respuesta.
     *
     * @param assetCode código patrimonial normalizado de la máquina solicitada
     * @return DTO público de la máquina encontrada
     * @throws MachineNotFoundException si no existe una máquina con el código indicado
     */
    @Transactional(readOnly = true)
    public MachineResponse getMachineByAssetCode(String assetCode) {
        Machine machine = machineRepository.findByAssetCode(assetCode)
                .orElseThrow(() -> new MachineNotFoundException(
                        "Máquina con " + assetCode + " no encontrada."
                ));

        return MachineMapper.toResponse(machine);
    }

    /**
     * Obtiene una página de máquinas según su estado operativo.
     *
     * <p>Conserva el orden ascendente por identificador para que los resultados sean estables
     * entre páginas.</p>
     *
     * @param page índice de página basado en cero
     * @param size número de máquinas por página
     * @param status estado con el que filtrar las máquinas
     * @return página de máquinas con el estado indicado; su contenido puede estar vacío
     */
    @Transactional(readOnly = true)
    public PageResponse<MachineResponse> getMachinesByStatus(
            int page,
            int size,
            MachineStatus status
    ) {
        Page<Machine> machines = machineRepository.findByStatus(
                status,
                PageRequest.of(page, size, Sort.by("id").ascending())
        );

        return new PageResponse<>(
                machines.getContent()
                        .stream()
                        .map(MachineMapper::toResponse)
                        .toList(),
                machines.getNumber(),
                machines.getSize(),
                machines.getTotalElements(),
                machines.getTotalPages()
        );
    }
}
