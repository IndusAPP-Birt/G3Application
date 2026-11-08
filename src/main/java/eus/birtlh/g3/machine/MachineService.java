package eus.birtlh.g3.machine;

import eus.birtlh.g3.common.dto.CatalogOptionResponse;
import eus.birtlh.g3.common.dto.PageResponse;
import eus.birtlh.g3.machine.dto.AdminCreateMachineRequest;
import eus.birtlh.g3.machine.dto.AdminUpdateAssetCodeRequest;
import eus.birtlh.g3.machine.dto.AdminUpdateMachineRequest;
import eus.birtlh.g3.machine.dto.MachineResponse;
import eus.birtlh.g3.machine.exception.AssetCodeAlreadyInUseException;
import eus.birtlh.g3.machine.exception.MachineNotFoundException;
import eus.birtlh.g3.machine.exception.SerialNumberAlreadyInUseException;
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

    /**
     * Registra una máquina a partir de los datos validados de una solicitud administrativa.
     *
     * <p>Comprueba la unicidad del código patrimonial y, si se ha indicado, del número de serie.
     * La máquina se crea inicialmente en estado {@link MachineStatus#ACTIVE}.</p>
     *
     * @param request datos normalizados y validados de la máquina que se desea registrar
     * @return DTO público de la máquina creada
     * @throws AssetCodeAlreadyInUseException si ya existe una máquina con el código patrimonial
     *                                        indicado
     * @throws SerialNumberAlreadyInUseException si el número de serie indicado ya está asignado
     */
    @Transactional
    public MachineResponse createMachine(AdminCreateMachineRequest request) {
        if (machineRepository.existsByAssetCode(request.assetCode())) {
            throw new AssetCodeAlreadyInUseException(
                    "El código patrimonial " + request.assetCode() + " ya existe."
            );
        }

        if (
                request.serialNumber() != null &&
                machineRepository.existsBySerialNumber(request.serialNumber())
        ) {
            throw new SerialNumberAlreadyInUseException(
                    "El número de serie " + request.serialNumber() + " ya existe."
            );
        }

        Machine machine = machineRepository.save(
                new Machine(
                        request.assetCode(),
                        request.name(),
                        request.manufacturer(),
                        request.model(),
                        request.serialNumber(),
                        request.location(),
                        request.commissionedAt()
                )
        );

        return MachineMapper.toResponse(machine);
    }

    /**
     * Actualiza los datos administrativos ordinarios de una máquina.
     *
     * <p>El código patrimonial se corrige mediante un caso de uso independiente y la fecha de
     * puesta en servicio permanece inmutable.</p>
     *
     * <p>Comprueba que el número de serie, si se ha indicado, no pertenezca a otra máquina.
     * Fuerza la sincronización con la base de datos antes de construir la respuesta para incluir
     * la fecha {@code updatedAt} actualizada.</p>
     *
     * @param id identificador de la máquina que se desea actualizar
     * @param request datos normalizados y validados que sustituyen los datos actualizables
     * @return DTO público de la máquina actualizada
     * @throws MachineNotFoundException si no existe una máquina con el identificador indicado
     * @throws SerialNumberAlreadyInUseException si el número de serie pertenece a otra máquina
     */
    @Transactional
    public MachineResponse updateMachine(Long id, AdminUpdateMachineRequest request) {
        Machine machine = machineRepository.findById(id)
                .orElseThrow(() -> new MachineNotFoundException(
                        "Máquina con " + id + " no encontrada."
                ));

        if (
                request.serialNumber() != null &&
                        machineRepository.existsBySerialNumberAndIdNot(request.serialNumber(), id)
        ) {
            throw new SerialNumberAlreadyInUseException(
                    "El número de serie " + request.serialNumber() + " ya existe."
            );
        }

        machine.changeName(request.name());
        machine.changeManufacturer(request.manufacturer());
        machine.changeModel(request.model());
        machine.changeSerialNumber(request.serialNumber());
        machine.changeLocation(request.location());
        machine.changeStatus(request.status());

        machineRepository.flush();

        return MachineMapper.toResponse(machine);
    }

    /**
     * Corrige el código patrimonial de una máquina registrada.
     *
     * <p>Comprueba que el código normalizado no esté asignado a otra máquina. Fuerza la
     * sincronización con la base de datos antes de construir la respuesta para incluir la fecha
     * {@code updatedAt} actualizada.</p>
     *
     * @param id identificador de la máquina cuyo código se desea corregir
     * @param request datos normalizados y validados con el nuevo código patrimonial
     * @return DTO público de la máquina actualizada
     * @throws MachineNotFoundException si no existe una máquina con el identificador indicado
     * @throws AssetCodeAlreadyInUseException si el código patrimonial pertenece a otra máquina
     */
    @Transactional
    public MachineResponse updateMachineAssetCode(
            Long id,
            AdminUpdateAssetCodeRequest request
    ) {
        Machine machine = machineRepository.findById(id)
                .orElseThrow(() -> new MachineNotFoundException(
                        "Máquina con " + id + " no encontrada."
                ));

        if (machineRepository.existsByAssetCodeAndIdNot(request.assetCode(), id)) {
            throw new AssetCodeAlreadyInUseException(
                    "El código patrimonial " + request.assetCode() + " ya existe."
            );
        }

        machine.changeAssetCode(request.assetCode());

        machineRepository.flush();

        return MachineMapper.toResponse(machine);
    }
}
