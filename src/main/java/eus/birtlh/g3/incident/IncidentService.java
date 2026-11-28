package eus.birtlh.g3.incident;

import eus.birtlh.g3.common.dto.CatalogOptionResponse;
import eus.birtlh.g3.common.dto.PageResponse;
import eus.birtlh.g3.incident.dto.AssignMaintenanceTechnicianToIncidentRequest;
import eus.birtlh.g3.incident.dto.CreateIncidentRequest;
import eus.birtlh.g3.incident.dto.IncidentResponse;
import eus.birtlh.g3.incident.event.IncidentAssignedEvent;
import eus.birtlh.g3.incident.exception.*;
import eus.birtlh.g3.incident.mapper.IncidentMapper;
import eus.birtlh.g3.incident.mapper.IncidentSeverityMapper;
import eus.birtlh.g3.incident.mapper.IncidentStatusMapper;
import eus.birtlh.g3.incident.repository.IncidentRepository;
import eus.birtlh.g3.preventive.Preventive;
import eus.birtlh.g3.preventive.PreventiveStatus;
import eus.birtlh.g3.preventive.event.PreventiveAssignedEvent;
import eus.birtlh.g3.preventive.exceptions.assignment.InspectorNotAssignedToPreventiveException;
import eus.birtlh.g3.preventive.exceptions.execution.InvalidPreventiveStatusTransitionException;
import eus.birtlh.g3.preventive.exceptions.execution.PreventiveNotFoundException;
import eus.birtlh.g3.preventive.repository.PreventiveRepository;
import eus.birtlh.g3.user.Role;
import eus.birtlh.g3.user.User;
import eus.birtlh.g3.user.exception.UserNotFoundException;
import eus.birtlh.g3.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * Servicio de aplicación que coordina los casos de uso de incidencias.
 *
 * <p>Expone catálogos, consultas paginadas y operaciones del ciclo de trabajo de incidencias.</p>
 */
@Service
@RequiredArgsConstructor
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final PreventiveRepository preventiveRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * Obtiene todos los niveles de gravedad disponibles como opciones de catálogo públicas.
     *
     * @return lista de niveles de gravedad con su código y descripción
     */
    public List<CatalogOptionResponse> getAllSeverities() {
        return Arrays.stream(IncidentSeverity.values())
                .map(IncidentSeverityMapper::toResponse)
                .toList();
    }

    /**
     * Obtiene todos los estados de incidencias disponibles como opciones de catálogo públicas.
     *
     * @return lista de estados disponibles con su código y descripción
     */
    public List<CatalogOptionResponse> getAllStatuses() {
        return Arrays.stream(IncidentStatus.values())
                .map(IncidentStatusMapper::toResponse)
                .toList();
    }

    /**
     * Obtiene una página de incidencias como DTOs de respuesta.
     *
     * <p>Conserva el orden ascendente por identificador para ofrecer resultados estables entre
     * páginas. La transacción mantiene disponibles el preventivo y los usuarios relacionados
     * durante la conversión mediante {@link IncidentMapper}.</p>
     *
     * @param page índice de página basado en cero
     * @param size número de incidencias por página
     * @return página de incidencias públicas; su contenido puede estar vacío
     */
    @Transactional(readOnly = true)
    public PageResponse<IncidentResponse> getAllIncidents(int page, int size) {
        Page<Incident> incidents = incidentRepository.findAll(
                PageRequest.of(page, size, Sort.by("id").ascending())
        );

        return new PageResponse<>(
                incidents.getContent()
                        .stream()
                        .map(IncidentMapper::toResponse)
                        .toList(),
                incidents.getNumber(),
                incidents.getSize(),
                incidents.getTotalElements(),
                incidents.getTotalPages()
        );
    }

    /**
     * Obtiene una incidencia por su identificador como DTO de respuesta.
     *
     * @param id identificador de la incidencia solicitada
     * @return DTO público de la incidencia encontrada
     * @throws IncidentNotFoundException si no existe una incidencia con el identificador indicado
     */
    @Transactional(readOnly = true)
    public IncidentResponse getIncidentById(Long id) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new IncidentNotFoundException(
                        "Incidencia con id " + id + " no encontrada."
                ));

        return IncidentMapper.toResponse(incident);
    }

    /**
     * Obtiene una página de incidencias asociadas a un preventivo.
     *
     * <p>La autorización de la consulta corresponde a la configuración de seguridad. Tanto la
     * administración como los inspectores autorizados pueden consultar las incidencias de
     * cualquier preventivo. Antes de realizar la consulta, se valida que el preventivo exista.</p>
     *
     * @param page índice de página basado en cero
     * @param size número de incidencias por página
     * @param preventiveId identificador del preventivo solicitado
     * @return página de incidencias públicas del preventivo; su contenido puede estar vacío
     * @throws PreventiveNotFoundException si no existe el preventivo indicado
     */
    @Transactional(readOnly = true)
    public PageResponse<IncidentResponse> getIncidentsByPreventiveId(
            int page,
            int size,
            Long preventiveId
    ) {
        if (!preventiveRepository.existsById(preventiveId)) {
            throw new PreventiveNotFoundException(
                    "Preventivo con id " + preventiveId + " no encontrado."
            );
        }

        Page<Incident> incidents = incidentRepository.findAllByPreventiveId(
                preventiveId,
                PageRequest.of(page, size, Sort.by("id").ascending())
        );

        return new PageResponse<>(
                incidents.getContent()
                        .stream()
                        .map(IncidentMapper::toResponse)
                        .toList(),
                incidents.getNumber(),
                incidents.getSize(),
                incidents.getTotalElements(),
                incidents.getTotalPages()
        );
    }

    /**
     * Obtiene una página de incidencias asignadas a un técnico de mantenimiento.
     *
     * <p>El correo recibido identifica al técnico autenticado y se utiliza para limitar el
     * resultado a su propia bandeja de trabajo.</p>
     *
     * @param page índice de página basado en cero
     * @param size número de incidencias por página
     * @param technicianEmail correo electrónico del técnico asignado
     * @return página de incidencias asignadas al técnico; su contenido puede estar vacío
     */
    @Transactional(readOnly = true)
    public PageResponse<IncidentResponse> getIncidentsByAssignedTechnicianEmail(
            int page,
            int size,
            String technicianEmail
    ) {
        Page<Incident> incidents = incidentRepository.findByAssignedTechnicianEmail(
                technicianEmail,
                PageRequest.of(page, size, Sort.by("id").ascending())
        );

        return new PageResponse<>(
                incidents.getContent()
                        .stream()
                        .map(IncidentMapper::toResponse)
                        .toList(),
                incidents.getNumber(),
                incidents.getSize(),
                incidents.getTotalElements(),
                incidents.getTotalPages()
        );
    }

    /**
     * Obtiene una página de incidencias asignadas a un técnico concreto.
     *
     * <p>Conserva el orden ascendente por identificador para ofrecer resultados estables entre
     * páginas.</p>
     *
     * @param page índice de página basado en cero
     * @param size número de incidencias por página
     * @param technicianId identificador del técnico asignado a las incidencias
     * @return página de incidencias asignadas al técnico; su contenido puede estar vacío
     */
    @Transactional(readOnly = true)
    public PageResponse<IncidentResponse> getIncidentsByAssignedTechnicianId(
            int page,
            int size,
            Long technicianId
    ) {
        Page<Incident> incidents = incidentRepository.findByAssignedTechnicianId(
                technicianId,
                PageRequest.of(page, size, Sort.by("id").ascending())
        );

        return new PageResponse<>(
                incidents.getContent()
                        .stream()
                        .map(IncidentMapper::toResponse)
                        .toList(),
                incidents.getNumber(),
                incidents.getSize(),
                incidents.getTotalElements(),
                incidents.getTotalPages()
        );
    }

    /**
     * Obtiene una página de incidencias comunicadas por un inspector.
     *
     * <p>El correo recibido identifica al inspector autenticado y limita el resultado a las
     * incidencias que ha comunicado, incluidas las resueltas pendientes de validación y cierre.</p>
     *
     * @param page índice de página basado en cero
     * @param size número de incidencias por página
     * @param inspectorEmail correo electrónico del inspector que comunicó las incidencias
     * @return página de incidencias comunicadas por el inspector; su contenido puede estar vacío
     */
    @Transactional(readOnly = true)
    public PageResponse<IncidentResponse> getIncidentsByReportedByInspectorEmail(
            int page,
            int size,
            String inspectorEmail
    ) {
        Page<Incident> incidents = incidentRepository.findByReportedByInspectorEmail(
                inspectorEmail,
                PageRequest.of(page, size, Sort.by("id").ascending())
        );

        return new PageResponse<>(
                incidents.getContent()
                        .stream()
                        .map(IncidentMapper::toResponse)
                        .toList(),
                incidents.getNumber(),
                incidents.getSize(),
                incidents.getTotalElements(),
                incidents.getTotalPages()
        );
    }

    /**
     * Obtiene una página de incidencias comunicadas por un inspector concreto.
     *
     * <p>Conserva el orden ascendente por identificador para ofrecer resultados estables entre
     * páginas.</p>
     *
     * @param page índice de página basado en cero
     * @param size número de incidencias por página
     * @param inspectorId identificador del inspector que comunicó las incidencias
     * @return página de incidencias comunicadas por el inspector; su contenido puede estar vacío
     */
    @Transactional(readOnly = true)
    public PageResponse<IncidentResponse> getIncidentsByReportedByInspectorId(
            int page,
            int size,
            Long inspectorId
    ) {
        Page<Incident> incidents = incidentRepository.findByReportedByInspectorId(
                inspectorId,
                PageRequest.of(page, size, Sort.by("id").ascending())
        );

        return new PageResponse<>(
                incidents.getContent()
                        .stream()
                        .map(IncidentMapper::toResponse)
                        .toList(),
                incidents.getNumber(),
                incidents.getSize(),
                incidents.getTotalElements(),
                incidents.getTotalPages()
        );
    }

    /**
     * Obtiene una página de incidencias filtradas por estado.
     *
     * <p>Conserva el orden ascendente por identificador para ofrecer resultados estables entre
     * páginas.</p>
     *
     * @param page índice de página basado en cero
     * @param size número de incidencias por página
     * @param status estado por el que se filtran las incidencias
     * @return página de incidencias con el estado indicado; su contenido puede estar vacío
     */
    @Transactional(readOnly = true)
    public PageResponse<IncidentResponse> getIncidentsByStatus(
            int page,
            int size,
            IncidentStatus status
    ) {
        Page<Incident> incidents = incidentRepository.findByStatus(
                status,
                PageRequest.of(page, size, Sort.by("id").ascending())
        );

        return new PageResponse<>(
                incidents.getContent()
                        .stream()
                        .map(IncidentMapper::toResponse)
                        .toList(),
                incidents.getNumber(),
                incidents.getSize(),
                incidents.getTotalElements(),
                incidents.getTotalPages()
        );
    }

    /**
     * Obtiene una página de incidencias filtradas por gravedad.
     *
     * <p>Conserva el orden ascendente por identificador para ofrecer resultados estables entre
     * páginas.</p>
     *
     * @param page índice de página basado en cero
     * @param size número de incidencias por página
     * @param severity nivel de gravedad por el que se filtran las incidencias
     * @return página de incidencias con la gravedad indicada; su contenido puede estar vacío
     */
    @Transactional(readOnly = true)
    public PageResponse<IncidentResponse> getIncidentsBySeverity(
            int page,
            int size,
            IncidentSeverity severity
    ) {
        Page<Incident> incidents = incidentRepository.findBySeverity(
                severity,
                PageRequest.of(page, size, Sort.by("id").ascending())
        );

        return new PageResponse<>(
                incidents.getContent()
                        .stream()
                        .map(IncidentMapper::toResponse)
                        .toList(),
                incidents.getNumber(),
                incidents.getSize(),
                incidents.getTotalElements(),
                incidents.getTotalPages()
        );
    }

    /**
     * Crea una incidencia abierta en un preventivo que está en ejecución.
     *
     * <p>El correo autenticado identifica al inspector que comunica la incidencia. Solo se
     * permite la creación si dicho inspector es el asignado al preventivo y este se encuentra en
     * {@link PreventiveStatus#IN_PROGRESS}. La incidencia se persiste inicialmente con estado
     * {@link IncidentStatus#OPEN}.</p>
     *
     * @param preventiveId identificador del preventivo en el que se ha detectado el problema
     * @param request descripción y gravedad ya validadas de la incidencia
     * @param authenticatedInspectorEmail correo del inspector autenticado
     * @return representación pública de la incidencia creada
     * @throws PreventiveNotFoundException si no existe el preventivo indicado
     * @throws UserNotFoundException si no existe el inspector autenticado
     * @throws InspectorNotAssignedToPreventiveException si el inspector no está asignado al
     *                                                    preventivo
     * @throws InvalidPreventiveStatusTransitionException si el preventivo no está en progreso
     */
    @Transactional
    public IncidentResponse createIncident(
        long preventiveId,
        CreateIncidentRequest request,
        String authenticatedInspectorEmail
    ) {
        Preventive preventive = preventiveRepository.findById(preventiveId)
                .orElseThrow(() -> new PreventiveNotFoundException(
                        "Preventivo con id " + preventiveId + " no encontrado."
                ));

        User authenticatedInspector = userRepository.findByEmail(authenticatedInspectorEmail)
                .orElseThrow(() -> new UserNotFoundException(
                        "Inspector autenticado no encontrado."
                ));

        if (
                preventive.getAssignedInspector() == null
                || !preventive.getAssignedInspector().getId().equals(authenticatedInspector.getId())
        ) {
            throw new InspectorNotAssignedToPreventiveException(
                    "El preventivo no está asignado al inspector autenticado."
            );
        }

        if (preventive.getStatus() != PreventiveStatus.IN_PROGRESS) {
            throw new InvalidPreventiveStatusTransitionException(
                    "Solo se pueden comunicar incidencias en un preventivo en progreso."
            );
        }

        Incident incident = incidentRepository.save(
                new Incident(
                        preventive,
                        request.description(),
                        request.severity(),
                        authenticatedInspector
                )
        );

        incidentRepository.flush();

        return IncidentMapper.toResponse(incident);
    }

    /**
     * Asigna una incidencia abierta a un técnico de mantenimiento habilitado.
     *
     * <p>Solo el inspector que comunicó la incidencia puede realizar la asignación. El usuario
     * seleccionado debe tener el rol {@link Role#ROLE_MAINTENANCE_TECHNICIAN} y estar habilitado.
     * La operación cambia el estado de {@link IncidentStatus#OPEN} a
     * {@link IncidentStatus#ASSIGNED}.</p>
     *
     * @param id identificador de la incidencia que se quiere asignar
     * @param request identificador del técnico de mantenimiento seleccionado
     * @param authenticatedInspectorEmail correo del inspector autenticado
     * @return representación pública de la incidencia con el técnico asignado
     * @throws IncidentNotFoundException si no existe la incidencia indicada
     * @throws UserNotFoundException si no existe el inspector autenticado o el técnico indicado
     * @throws IncidentAccessDeniedException si el inspector no comunicó la incidencia
     * @throws InvalidIncidentStatusTransitionException si la incidencia no está abierta
     * @throws UserIsNotMaintenanceTechnicianException si el usuario no es un técnico habilitado
     */
    @Transactional
    public IncidentResponse assignMaintenanceTechnicianToIncident(
        long id,
        AssignMaintenanceTechnicianToIncidentRequest request,
        String authenticatedInspectorEmail
    ) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new IncidentNotFoundException(
                        "Incidencia con id " + id + " no encontrada."
                ));

        User inspector = userRepository.findByEmail(authenticatedInspectorEmail)
                .orElseThrow(() -> new UserNotFoundException(
                        "Inspector autenticado no encontrado."
                ));

        if (!incident.getReportedByInspector().getId().equals(inspector.getId())) {
            throw new IncidentAccessDeniedException(
                    "Solo el inspector que comunicó la incidencia puede asignarla."
            );
        }

        if (incident.getStatus() != IncidentStatus.OPEN) {
            throw new InvalidIncidentStatusTransitionException(
                    "Solo se puede asignar un técnico a una incidencia abierta."
            );
        }

        User maintenanceTechnician = userRepository.findById(request.maintenanceTechnicianId())
                .orElseThrow(() -> new UserNotFoundException(
                        "Usuario con " + request.maintenanceTechnicianId() + " no encontrado."
                ));

        if (maintenanceTechnician.getRole() != Role.ROLE_MAINTENANCE_TECHNICIAN) {
            throw new UserIsNotMaintenanceTechnicianException(
                    "El usuario indicado no tiene rol de técnico de mantenimiento."
            );
        }

        if (!maintenanceTechnician.isEnabled()) {
            throw new UserIsNotMaintenanceTechnicianException(
                    "El técnico de mantenimiento indicado no está habilitado."
            );
        }

        incident.assignTechnician(maintenanceTechnician);
        incidentRepository.flush();

        applicationEventPublisher.publishEvent(
                new IncidentAssignedEvent(incident.getId(), maintenanceTechnician.getId())
        );

        return IncidentMapper.toResponse(incident);
    }

    /**
     * Inicia la reparación de una incidencia asignada.
     *
     * <p>Solo el técnico de mantenimiento actualmente asignado puede iniciar la incidencia. La
     * operación cambia el estado de {@link IncidentStatus#ASSIGNED} a
     * {@link IncidentStatus#IN_PROGRESS} y registra su momento de inicio.</p>
     *
     * @param id identificador de la incidencia que se quiere iniciar
     * @param authenticatedMaintenanceTechnicianEmail correo del técnico autenticado
     * @return representación pública de la incidencia iniciada
     * @throws IncidentNotFoundException si no existe la incidencia indicada
     * @throws UserNotFoundException si no existe el técnico autenticado
     * @throws MaintenanceTechnicianNotAssignedToIncidentException si el técnico no está asignado
     *                                                              a la incidencia
     * @throws InvalidIncidentStatusTransitionException si la incidencia no está asignada
     */
    @Transactional
    public IncidentResponse startIncident(
        long id,
        String authenticatedMaintenanceTechnicianEmail
    ) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new IncidentNotFoundException(
                        "Incidencia con id " + id + " no encontrada."
                ));

        validateMaintenanceTechnicianAssignment(incident, authenticatedMaintenanceTechnicianEmail);

        if (incident.getStatus() != IncidentStatus.ASSIGNED) {
            throw new InvalidIncidentStatusTransitionException(
                    "Solo se puede iniciar una incidencia asignada."
            );
        }

        incident.start();
        incidentRepository.flush();

        return IncidentMapper.toResponse(incident);
    }

    private void validateMaintenanceTechnicianAssignment(
            Incident incident,
            String authenticatedMaintenanceTechnicianEmail
    ) {
        User authenticatedMaintenanceTechnician = userRepository.findByEmail(authenticatedMaintenanceTechnicianEmail)
                .orElseThrow(() -> new UserNotFoundException(
                        "Técnico de mantenimiento no encontrado."
                ));

        if (
                incident.getAssignedTechnician() == null
                || !incident.getAssignedTechnician().getId().equals(authenticatedMaintenanceTechnician.getId())
        ) {
            throw new MaintenanceTechnicianNotAssignedToIncidentException(
                    "La incidencia no está asignada al técnico de mantenimiento autenticado."
            );
        }
    }
}
