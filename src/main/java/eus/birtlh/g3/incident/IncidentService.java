package eus.birtlh.g3.incident;

import eus.birtlh.g3.common.dto.CatalogOptionResponse;
import eus.birtlh.g3.common.dto.PageResponse;
import eus.birtlh.g3.incident.dto.IncidentResponse;
import eus.birtlh.g3.incident.exception.IncidentNotFoundException;
import eus.birtlh.g3.incident.mapper.IncidentMapper;
import eus.birtlh.g3.incident.mapper.IncidentSeverityMapper;
import eus.birtlh.g3.incident.mapper.IncidentStatusMapper;
import eus.birtlh.g3.incident.repository.IncidentRepository;
import eus.birtlh.g3.preventive.exceptions.execution.PreventiveNotFoundException;
import eus.birtlh.g3.preventive.repository.PreventiveRepository;
import lombok.RequiredArgsConstructor;
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
 * <p>Expone los catálogos y las consultas paginadas que utilizan los clientes de la API para
 * consultar incidencias.</p>
 */
@Service
@RequiredArgsConstructor
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final PreventiveRepository preventiveRepository;

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
}
