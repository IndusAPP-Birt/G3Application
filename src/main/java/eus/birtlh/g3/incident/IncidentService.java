package eus.birtlh.g3.incident;

import eus.birtlh.g3.common.dto.CatalogOptionResponse;
import eus.birtlh.g3.incident.mapper.IncidentSeverityMapper;
import eus.birtlh.g3.incident.mapper.IncidentStatusMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}
