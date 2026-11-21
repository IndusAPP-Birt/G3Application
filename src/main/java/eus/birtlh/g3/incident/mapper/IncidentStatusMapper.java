package eus.birtlh.g3.incident.mapper;

import eus.birtlh.g3.common.dto.CatalogOptionResponse;
import eus.birtlh.g3.incident.IncidentStatus;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Convierte estados de incidencias en opciones legibles de catálogo.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class IncidentStatusMapper {

    /**
     * Transforma un estado de incidencia en su código y descripción legible.
     *
     * @param status estado que se desea exponer
     * @return opción de catálogo correspondiente al estado
     * @throws IllegalArgumentException si el estado es {@code null}
     */
    public static CatalogOptionResponse toResponse(IncidentStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("El estado de la incidencia es obligatorio.");
        }

        String description = switch (status) {
            case OPEN -> "Abierta";
            case ASSIGNED -> "Asignada";
            case IN_PROGRESS -> "En progreso";
            case RESOLVED -> "Resuelta";
            case CLOSED -> "Cerrada";
        };

        return new CatalogOptionResponse(status.name(), description);
    }
}
