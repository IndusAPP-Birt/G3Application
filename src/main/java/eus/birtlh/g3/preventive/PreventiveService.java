package eus.birtlh.g3.preventive;

import eus.birtlh.g3.common.dto.PageResponse;
import eus.birtlh.g3.preventive.dto.AdminAssignInspectorToPreventiveRequest;
import eus.birtlh.g3.preventive.dto.PreventiveResponse;
import eus.birtlh.g3.preventive.dto.RecordPreventiveWorkRequest;
import eus.birtlh.g3.preventive.exceptions.InvalidPreventiveDateRangeException;
import eus.birtlh.g3.preventive.exceptions.InvalidPreventiveStatusTransitionException;
import eus.birtlh.g3.preventive.exceptions.PreventiveNotFoundException;
import eus.birtlh.g3.preventive.exceptions.PreventiveWorkReportRequiredException;
import eus.birtlh.g3.preventive.exceptions.UserIsNotInspectorException;
import eus.birtlh.g3.preventive.mapper.PreventiveMapper;
import eus.birtlh.g3.preventive.repository.PreventiveRepository;
import eus.birtlh.g3.user.Role;
import eus.birtlh.g3.user.User;
import eus.birtlh.g3.user.exception.UserNotFoundException;
import eus.birtlh.g3.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Servicio de aplicación que coordina los casos de uso de ejecuciones preventivas.
 *
 * <p>Obtiene entidades {@link Preventive} y las transforma en DTOs públicos antes de devolverlas
 * a la capa HTTP.</p>
 */
@Service
@RequiredArgsConstructor
public class PreventiveService {

    private final PreventiveRepository preventiveRepository;
    private final UserRepository userRepository;

    /**
     * Obtiene una página de preventivos como DTOs de respuesta.
     *
     * <p>Conserva el orden ascendente por identificador para que los resultados sean estables
     * entre páginas. La transacción mantiene disponibles la asignación de origen y el inspector
     * asociado durante la conversión mediante {@link PreventiveMapper}.</p>
     *
     * @param page índice de página basado en cero
     * @param size número de preventivos por página
     * @return página de preventivos públicos; su contenido puede estar vacío
     */
    @Transactional(readOnly = true)
    public PageResponse<PreventiveResponse> getAllPreventives(int page, int size) {
        Page<Preventive> preventives = preventiveRepository.findAll(
                PageRequest.of(page, size, Sort.by("id").ascending())
        );

        return new PageResponse<>(
                preventives.getContent()
                        .stream()
                        .map(PreventiveMapper::toResponse)
                        .toList(),
                preventives.getNumber(),
                preventives.getSize(),
                preventives.getTotalElements(),
                preventives.getTotalPages()
        );
    }

    /**
     * Obtiene un preventivo por su identificador como DTO de respuesta.
     *
     * @param id identificador del preventivo solicitado
     * @return DTO público del preventivo encontrado
     * @throws PreventiveNotFoundException si no existe un preventivo con el identificador indicado
     */
    @Transactional(readOnly = true)
    public PreventiveResponse getPreventiveById(Long id) {
        Preventive preventive = preventiveRepository.findById(id)
                .orElseThrow(() -> new PreventiveNotFoundException(
                        "Preventivo con id " + id + " no encontrado."
                ));

        return PreventiveMapper.toResponse(preventive);
    }

    /**
     * Obtiene una página de preventivos asociados a una plantilla preventiva.
     *
     * <p>Conserva el orden ascendente por identificador para que los resultados sean estables
     * entre páginas.</p>
     *
     * @param page índice de página basado en cero
     * @param size número de preventivos por página
     * @param preventivePlanId identificador de la plantilla preventiva con la que filtrar
     * @return página de preventivos de la plantilla; su contenido puede estar vacío
     */
    @Transactional(readOnly = true)
    public PageResponse<PreventiveResponse> getPreventivesByPreventivePlanId(
            int page,
            int size,
            long preventivePlanId
    ) {
        Page<Preventive> preventives = preventiveRepository.findByPreventivePlanAssignmentPreventivePlanId(
                preventivePlanId,
                PageRequest.of(page, size, Sort.by("id").ascending())
        );

        return new PageResponse<>(
                preventives.getContent()
                        .stream()
                        .map(PreventiveMapper::toResponse)
                        .toList(),
                preventives.getNumber(),
                preventives.getSize(),
                preventives.getTotalElements(),
                preventives.getTotalPages()
        );
    }

    /**
     * Obtiene una página de preventivos según su estado.
     *
     * <p>Conserva el orden ascendente por identificador para que los resultados sean estables
     * entre páginas.</p>
     *
     * @param page índice de página basado en cero
     * @param size número de preventivos por página
     * @param status estado con el que filtrar los preventivos
     * @return página de preventivos con el estado indicado; su contenido puede estar vacío
     */
    @Transactional(readOnly = true)
    public PageResponse<PreventiveResponse> getPreventivesByStatus(
            int page,
            int size,
            PreventiveStatus status
    ) {
        Page<Preventive> preventives = preventiveRepository.findByStatus(
                status,
                PageRequest.of(page, size, Sort.by("id").ascending())
        );

        return new PageResponse<>(
                preventives.getContent()
                        .stream()
                        .map(PreventiveMapper::toResponse)
                        .toList(),
                preventives.getNumber(),
                preventives.getSize(),
                preventives.getTotalElements(),
                preventives.getTotalPages()
        );
    }

    /**
     * Obtiene una página de preventivos asignados a un inspector.
     *
     * <p>Conserva el orden ascendente por identificador para que los resultados sean estables
     * entre páginas.</p>
     *
     * @param page índice de página basado en cero
     * @param size número de preventivos por página
     * @param inspectorId identificador del inspector con el que filtrar
     * @return página de preventivos asignados al inspector; su contenido puede estar vacío
     */
    @Transactional(readOnly = true)
    public PageResponse<PreventiveResponse> getPreventivesByAssignedInspectorId(
            int page,
            int size,
            long inspectorId
    ) {
        Page<Preventive> preventives = preventiveRepository.findByAssignedInspectorId(
                inspectorId,
                PageRequest.of(page, size, Sort.by("id").ascending())
        );

        return new PageResponse<>(
                preventives.getContent()
                        .stream()
                        .map(PreventiveMapper::toResponse)
                        .toList(),
                preventives.getNumber(),
                preventives.getSize(),
                preventives.getTotalElements(),
                preventives.getTotalPages()
        );
    }

    /**
     * Obtiene una página de preventivos cuya fecha prevista está comprendida en un rango de fechas
     * inclusivo.
     *
     * <p>El rango debe situar la fecha inicial antes o en la misma fecha que la final. Los
     * resultados se ordenan ascendentemente por identificador para que sean estables entre
     * páginas.</p>
     *
     * @param page índice de página basado en cero
     * @param size número de preventivos por página
     * @param from fecha inicial del rango, incluida
     * @param to fecha final del rango, incluida
     * @return página de preventivos previstos en el rango; su contenido puede estar vacío
     * @throws InvalidPreventiveDateRangeException si {@code from} es posterior a {@code to}
     */
    @Transactional(readOnly = true)
    public PageResponse<PreventiveResponse> getPreventivesByDueDate(
            int page,
            int size,
            LocalDate from,
            LocalDate to
    ) {
        if (from.isAfter(to)) {
            throw new InvalidPreventiveDateRangeException(
                    "La fecha inicial no puede ser posterior a la fecha final."
            );
        }

        Page<Preventive> preventives =
                preventiveRepository.findByDueDateBetween(
                        from,
                        to,
                        PageRequest.of(page, size, Sort.by("id").ascending())
                );

        return new PageResponse<>(
                preventives.getContent().stream()
                        .map(PreventiveMapper::toResponse)
                        .toList(),
                preventives.getNumber(),
                preventives.getSize(),
                preventives.getTotalElements(),
                preventives.getTotalPages()
        );
    }

    /**
     * Asigna un inspector a un preventivo pendiente.
     *
     * <p>Comprueba que el preventivo y el usuario existan, que este último tenga el rol de
     * inspector y que el preventivo esté pendiente. La entidad registra la asignación y cambia
     * su estado a {@link PreventiveStatus#ASSIGNED}.</p>
     *
     * @param id identificador del preventivo al que se asignará el inspector
     * @param request identificador validado del inspector que recibirá la asignación
     * @return DTO público del preventivo con el inspector asignado
     * @throws PreventiveNotFoundException si no existe un preventivo con el identificador indicado
     * @throws UserNotFoundException si no existe el usuario inspector indicado
     * @throws UserIsNotInspectorException si el usuario indicado no tiene el rol de inspector
     * @throws InvalidPreventiveStatusTransitionException si el preventivo no está pendiente
     */
    @Transactional
    public PreventiveResponse assignInspectorToPreventive(
            long id,
            AdminAssignInspectorToPreventiveRequest request
    ) {
        Preventive preventive = preventiveRepository.findById(id)
                .orElseThrow(() -> new PreventiveNotFoundException(
                        "Preventivo con " + id + " no encontrado."
                ));

        if (preventive.getStatus() != PreventiveStatus.PENDING) {
            throw new InvalidPreventiveStatusTransitionException(
                    "Solo se puede asignar un inspector a un preventivo pendiente."
            );
        }

        User inspector = userRepository.findById(request.inspectorId())
                .orElseThrow(() -> new UserNotFoundException(
                        "Usuario con " + request.inspectorId() + " no encontrado."
                ));

        if (inspector.getRole() != Role.ROLE_INSPECTOR) {
            throw new UserIsNotInspectorException(
                    "El usuario indicado no tiene el rol de inspector."
            );
        }

        preventive.assignInspector(inspector);
        preventiveRepository.flush();

        return PreventiveMapper.toResponse(preventive);
    }

    /**
     * Inicia un preventivo que ya tiene un inspector asignado.
     *
     * <p>Solo permite la transición desde {@link PreventiveStatus#ASSIGNED}. La entidad registra
     * el momento de inicio y cambia el estado a {@link PreventiveStatus#IN_PROGRESS}.</p>
     *
     * @param id identificador del preventivo que se desea iniciar
     * @return DTO público del preventivo iniciado
     * @throws PreventiveNotFoundException si no existe un preventivo con el identificador indicado
     * @throws InvalidPreventiveStatusTransitionException si el preventivo no está asignado
     */
    @Transactional
    public PreventiveResponse startPreventive(long id) {
        Preventive preventive = preventiveRepository.findById(id)
                .orElseThrow(() -> new PreventiveNotFoundException(
                        "Preventivo con " + id + " no encontrado."
                ));

        if (preventive.getStatus() != PreventiveStatus.ASSIGNED) {
            throw new InvalidPreventiveStatusTransitionException(
                    "Solo se puede iniciar un preventivo asignado."
            );
        }

        preventive.start();
        preventiveRepository.flush();

        return PreventiveMapper.toResponse(preventive);
    }

    /**
     * Registra o corrige el trabajo realizado en un preventivo en curso.
     *
     * <p>Solo permite registrar el informe mientras el preventivo está en
     * {@link PreventiveStatus#IN_PROGRESS}; esta operación no modifica su estado.</p>
     *
     * @param id identificador del preventivo en el que se registra el trabajo
     * @param request informe de trabajo validado y normalizado
     * @return DTO público del preventivo con el trabajo realizado actualizado
     * @throws PreventiveNotFoundException si no existe un preventivo con el identificador indicado
     * @throws InvalidPreventiveStatusTransitionException si el preventivo no está en progreso
     */
    @Transactional
    public PreventiveResponse recordWorkPerformed(
            long id,
            RecordPreventiveWorkRequest request
    ) {
        Preventive preventive = preventiveRepository.findById(id)
                .orElseThrow(() -> new PreventiveNotFoundException(
                        "Preventivo con " + id + " no encontrado."
                ));

        if (preventive.getStatus() != PreventiveStatus.IN_PROGRESS) {
            throw new InvalidPreventiveStatusTransitionException(
                    "Solo se puede guardar un trabajo en un preventivo en progreso."
            );
        }

        preventive.recordWorkPerformed(request.workPerformed());
        preventiveRepository.flush();

        return PreventiveMapper.toResponse(preventive);
    }

    /**
     * Completa un preventivo en curso que tiene trabajo registrado.
     *
     * <p>Solo permite la transición desde {@link PreventiveStatus#IN_PROGRESS}. La entidad
     * comprueba que exista un informe de trabajo antes de registrar la finalización y cambiar el
     * estado a {@link PreventiveStatus#COMPLETED}.</p>
     *
     * @param id identificador del preventivo que se desea completar
     * @return DTO público del preventivo completado
     * @throws PreventiveNotFoundException si no existe un preventivo con el identificador indicado
     * @throws InvalidPreventiveStatusTransitionException si el preventivo no está en progreso
     * @throws PreventiveWorkReportRequiredException si no se ha registrado trabajo realizado
     */
    @Transactional
    public PreventiveResponse completePreventive(long id) {
        Preventive preventive = preventiveRepository.findById(id)
                .orElseThrow(() -> new PreventiveNotFoundException(
                        "Preventivo con " + id + " no encontrado."
                ));

        if (preventive.getStatus() != PreventiveStatus.IN_PROGRESS) {
            throw new InvalidPreventiveStatusTransitionException(
                    "Solo se puede completar un preventivo en progreso."
            );
        }

        preventive.complete();
        preventiveRepository.flush();

        return PreventiveMapper.toResponse(preventive);
    }
}
