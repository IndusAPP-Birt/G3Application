package eus.birtlh.g3.incident.mapper;

import eus.birtlh.g3.incident.Incident;
import eus.birtlh.g3.incident.dto.IncidentResponse;
import eus.birtlh.g3.preventive.mapper.PreventiveMapper;
import eus.birtlh.g3.user.mapper.UserMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Convierte entidades {@link Incident} persistidas en sus DTOs públicos de respuesta.
 *
 * <p>Transforma el preventivo, los usuarios relacionados y los valores de catálogo de la
 * incidencia mediante sus mappers correspondientes. Cuando no existe técnico asignado, la
 * respuesta conserva ese valor como {@code null}.</p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class IncidentMapper {

    /**
     * Transforma una incidencia persistida en su representación pública.
     *
     * @param incident incidencia que se desea exponer
     * @return DTO público correspondiente a la incidencia
     * @throws IllegalArgumentException si la incidencia es {@code null} o no está persistida
     */
    public static IncidentResponse toResponse(Incident incident) {
        if (incident == null) {
            throw new IllegalArgumentException("La incidencia es obligatoria.");
        }

        if (incident.getId() == null) {
            throw new IllegalArgumentException("La incidencia debe estar persistida.");
        }

        return new IncidentResponse(
                incident.getId(),
                PreventiveMapper.toResponse(incident.getPreventive()),
                incident.getDescription(),
                IncidentSeverityMapper.toResponse(incident.getSeverity()),
                IncidentStatusMapper.toResponse(incident.getStatus()),
                UserMapper.toResponse(incident.getReportedByInspector()),
                incident.getAssignedTechnician() == null
                ? null : UserMapper.toResponse(incident.getAssignedTechnician()),
                incident.getAssignedAt(),
                incident.getStartedAt(),
                incident.getResolvedAt(),
                incident.getClosedAt(),
                incident.getCreatedAt(),
                incident.getUpdatedAt()

        );
    }
}
