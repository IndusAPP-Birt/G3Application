package eus.birtlh.g3.incident.mapper;

import eus.birtlh.g3.common.dto.CatalogOptionResponse;
import eus.birtlh.g3.incident.IncidentSeverity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Convierte niveles de gravedad de incidencias en opciones legibles de catálogo.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class IncidentSeverityMapper {

    /**
     * Transforma un nivel de gravedad de incidencia en su código y descripción legible.
     *
     * @param severity gravedad que se desea exponer
     * @return opción de catálogo correspondiente a la gravedad
     * @throws IllegalArgumentException si la gravedad es {@code null}
     */
    public static CatalogOptionResponse toResponse(IncidentSeverity severity) {
        if (severity == null) {
            throw new IllegalArgumentException("La gravedad de la incidencia es obligatoria.");
        }

        String description = switch (severity) {
            case LOW -> "Baja";
            case MEDIUM -> "Media";
            case HIGH -> "Alta";
            case CRITICAL -> "Crítica";
        };

        return new CatalogOptionResponse(severity.name(), description);
    }
}
