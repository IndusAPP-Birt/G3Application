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
 * <p>Expone los catálogos de estados y niveles de gravedad que utilizarán los clientes de la
 * API al gestionar incidencias.</p>
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
    public PageResponse<IncidentResponse> getAllIncidentsByPreventiveId(
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
}
