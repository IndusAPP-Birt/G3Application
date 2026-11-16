package eus.birtlh.g3.preventive;

import eus.birtlh.g3.common.dto.PageResponse;
import eus.birtlh.g3.machine.Machine;
import eus.birtlh.g3.machine.MachineStatus;
import eus.birtlh.g3.machine.exception.MachineNotActiveException;
import eus.birtlh.g3.machine.exception.MachineNotFoundException;
import eus.birtlh.g3.machine.repository.MachineRepository;
import eus.birtlh.g3.preventive.dto.assignment.CreatePreventivePlanAssignmentRequest;
import eus.birtlh.g3.preventive.dto.assignment.UpdatePreventivePlanAssignmentRequest;
import eus.birtlh.g3.preventive.dto.assignment.PreventivePlanAssignmentResponse;
import eus.birtlh.g3.preventive.exceptions.assignment.PreventivePlanAssignmentNotFoundException;
import eus.birtlh.g3.preventive.exceptions.plan.PreventivePlanInactiveException;
import eus.birtlh.g3.preventive.exceptions.plan.PreventivePlanNotFoundException;
import eus.birtlh.g3.preventive.mapper.PreventivePlanAssignmentMapper;
import eus.birtlh.g3.preventive.repository.PreventivePlanAssignmentRepository;
import eus.birtlh.g3.preventive.repository.PreventivePlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de aplicación que coordina los casos de uso de asignaciones de plantillas preventivas
 * a máquinas.
 *
 * <p>Gestiona el calendario y la activación de cada asignación, mientras que la plantilla y la
 * máquina relacionadas permanecen inmutables después de su creación.</p>
 */
@Service
@RequiredArgsConstructor
public class PreventivePlanAssignmentService {

    private final PreventivePlanAssignmentRepository preventivePlanAssignmentRepository;
    private final PreventivePlanRepository preventivePlanRepository;
    private final MachineRepository machineRepository;

    /**
     * Obtiene una página de asignaciones preventivas como DTOs de respuesta.
     *
     * <p>Conserva el orden ascendente por identificador para que los resultados sean estables
     * entre páginas.</p>
     *
     * @param page índice de página basado en cero
     * @param size número de asignaciones por página
     * @return página de asignaciones preventivas públicas; su contenido puede estar vacío
     */
    @Transactional(readOnly = true)
    public PageResponse<PreventivePlanAssignmentResponse> getAllPreventivePlanAssignments(int page, int size) {
        Page<PreventivePlanAssignment> preventivePlanAssignments = preventivePlanAssignmentRepository.findAll(
                PageRequest.of(page, size, Sort.by("id").ascending())
        );

        return new PageResponse<>(
                preventivePlanAssignments.getContent()
                        .stream()
                        .map(PreventivePlanAssignmentMapper::toResponse)
                        .toList(),
                preventivePlanAssignments.getNumber(),
                preventivePlanAssignments.getSize(),
                preventivePlanAssignments.getTotalElements(),
                preventivePlanAssignments.getTotalPages()
        );
    }

    /**
     * Obtiene una asignación preventiva por su identificador como DTO de respuesta.
     *
     * @param id identificador de la asignación preventiva solicitada
     * @return DTO público de la asignación encontrada
     * @throws PreventivePlanAssignmentNotFoundException si no existe una asignación con el
     *                                                    identificador indicado
     */
    @Transactional(readOnly = true)
    public PreventivePlanAssignmentResponse getPreventivePlanAssignmentById(Long id) {
        PreventivePlanAssignment preventivePlanAssignment = preventivePlanAssignmentRepository.findById(id)
                .orElseThrow(() -> new PreventivePlanAssignmentNotFoundException(
                        "Asignación de plan preventivo con id " + id + " no encontrada."
                ));

        return PreventivePlanAssignmentMapper.toResponse(preventivePlanAssignment);
    }

    /**
     * Obtiene una página de asignaciones preventivas según su estado de activación.
     *
     * <p>Conserva el orden ascendente por identificador para que los resultados sean estables
     * entre páginas.</p>
     *
     * @param page índice de página basado en cero
     * @param size número de asignaciones por página
     * @param active {@code true} para las asignaciones activas; {@code false} para las inactivas
     * @return página de asignaciones con el estado solicitado; su contenido puede estar vacío
     */
    @Transactional(readOnly = true)
    public PageResponse<PreventivePlanAssignmentResponse> getPreventivePlanAssignmentsByActive(
            int page,
            int size,
            boolean active
    ) {
        Page<PreventivePlanAssignment> preventivePlanAssignments = preventivePlanAssignmentRepository.findByActive(
                active,
                PageRequest.of(page, size, Sort.by("id").ascending())
        );

        return new PageResponse<>(
                preventivePlanAssignments.getContent()
                        .stream()
                        .map(PreventivePlanAssignmentMapper::toResponse)
                        .toList(),
                preventivePlanAssignments.getNumber(),
                preventivePlanAssignments.getSize(),
                preventivePlanAssignments.getTotalElements(),
                preventivePlanAssignments.getTotalPages()
        );
    }

    /**
     * Obtiene una página de asignaciones de planes preventivos configuradas para una máquina.
     *
     * <p>Conserva el orden ascendente por identificador para que los resultados sean estables
     * entre páginas.</p>
     *
     * @param page índice de página basado en cero
     * @param size número de asignaciones por página
     * @param machineId identificador de la máquina con la que filtrar
     * @return página de asignaciones de la máquina; su contenido puede estar vacío
     */
    @Transactional(readOnly = true)
    public PageResponse<PreventivePlanAssignmentResponse> getPreventivePlanAssignmentsByMachineId(
            int page,
            int size,
            long machineId
    ) {
        Page<PreventivePlanAssignment> preventivePlanAssignments = preventivePlanAssignmentRepository.findByMachineId(
                machineId,
                PageRequest.of(page, size, Sort.by("id").ascending())
        );

        return new PageResponse<>(
                preventivePlanAssignments.getContent()
                        .stream()
                        .map(PreventivePlanAssignmentMapper::toResponse)
                        .toList(),
                preventivePlanAssignments.getNumber(),
                preventivePlanAssignments.getSize(),
                preventivePlanAssignments.getTotalElements(),
                preventivePlanAssignments.getTotalPages()
        );
    }

    /**
     * Obtiene una página de asignaciones configuradas a partir de una plantilla preventiva.
     *
     * <p>Conserva el orden ascendente por identificador para que los resultados sean estables
     * entre páginas.</p>
     *
     * @param page índice de página basado en cero
     * @param size número de asignaciones por página
     * @param preventivePlanId identificador de la plantilla con la que filtrar
     * @return página de asignaciones de la plantilla; su contenido puede estar vacío
     */
    @Transactional(readOnly = true)
    public PageResponse<PreventivePlanAssignmentResponse> getPreventivePlanAssignmentsByPreventivePlanId(
            int page,
            int size,
            long preventivePlanId
    ) {
        Page<PreventivePlanAssignment> preventivePlanAssignments = preventivePlanAssignmentRepository.findByPreventivePlanId(
                preventivePlanId,
                PageRequest.of(page, size, Sort.by("id").ascending())
        );

        return new PageResponse<>(
                preventivePlanAssignments.getContent()
                        .stream()
                        .map(PreventivePlanAssignmentMapper::toResponse)
                        .toList(),
                preventivePlanAssignments.getNumber(),
                preventivePlanAssignments.getSize(),
                preventivePlanAssignments.getTotalElements(),
                preventivePlanAssignments.getTotalPages()
        );
    }

    /**
     * Asigna una plantilla preventiva a una máquina con su planificación inicial.
     *
     * <p>Comprueba que existan tanto la plantilla como la máquina y que ambas estén activas antes
     * de crear la asignación. Las dos relaciones quedan inmutables después de la creación.</p>
     *
     * @param request datos validados de la asignación y de su calendario inicial
     * @return DTO público de la asignación creada
     * @throws PreventivePlanNotFoundException si no existe la plantilla indicada en la solicitud
     * @throws PreventivePlanInactiveException si la plantilla indicada está inactiva
     * @throws MachineNotFoundException si no existe la máquina indicada en la solicitud
     * @throws MachineNotActiveException si la máquina indicada no está activa
     */
    @Transactional
    public PreventivePlanAssignmentResponse createPreventivePlanAssignment(CreatePreventivePlanAssignmentRequest request) {
        PreventivePlan preventivePlan = preventivePlanRepository.findById(request.preventivePlanId())
                .orElseThrow(() -> new PreventivePlanNotFoundException(
                        "Plan preventivo con id " + request.preventivePlanId() + " no encontrado."
                ));

        if (!preventivePlan.isActive()) {
            throw new PreventivePlanInactiveException(
                    "El plan preventivo seleccionado no está activo."
            );
        }

        Machine machine = machineRepository.findById(request.machineId())
                .orElseThrow(() -> new MachineNotFoundException(
                        "Máquina con id " + request.machineId() + " no encontrada."
                ));

        if (machine.getStatus() != MachineStatus.ACTIVE) {
            throw new MachineNotActiveException(
                    "La máquina seleccionada no está activa."
            );
        }

        PreventivePlanAssignment preventivePlanAssignment = preventivePlanAssignmentRepository.save(
                new PreventivePlanAssignment(
                        preventivePlan,
                        machine,
                        request.intervalDays(),
                        request.noticeDays(),
                        request.nextDueDate()
                )
        );

        return PreventivePlanAssignmentMapper.toResponse(preventivePlanAssignment);
    }

    /**
     * Actualiza el calendario y la activación de una asignación preventiva existente.
     *
     * <p>No modifica la plantilla ni la máquina asociadas, que son relaciones inmutables. La
     * próxima fecha se reprograma de forma explícita con el valor indicado en la solicitud.</p>
     *
     * @param id identificador de la asignación preventiva que se desea actualizar
     * @param request datos validados con los que actualizar el calendario y la activación
     * @return DTO público de la asignación actualizada
     * @throws PreventivePlanAssignmentNotFoundException si no existe una asignación con el
     *                                                    identificador indicado
     */
    @Transactional
    public PreventivePlanAssignmentResponse updatePreventivePlanAssignment(
            Long id,
            UpdatePreventivePlanAssignmentRequest request
    ) {
        PreventivePlanAssignment preventivePlanAssignment = preventivePlanAssignmentRepository.findById(id)
                .orElseThrow(() -> new PreventivePlanAssignmentNotFoundException(
                        "Asignación de plan preventivo con id " + id + " no encontrada."
                ));

        preventivePlanAssignment.changeIntervalDays(request.intervalDays());
        preventivePlanAssignment.changeNoticeDays(request.noticeDays());
        preventivePlanAssignment.scheduleNextDueDate(request.nextDueDate());

        if (request.active()) {
            preventivePlanAssignment.activate();
        } else {
            preventivePlanAssignment.deactivate();
        }

        return PreventivePlanAssignmentMapper.toResponse(preventivePlanAssignment);
    }
}
