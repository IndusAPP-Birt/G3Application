package eus.birtlh.g3.incident.dto;

import eus.birtlh.g3.common.dto.CatalogOptionResponse;
import eus.birtlh.g3.preventive.dto.execution.PreventiveResponse;
import eus.birtlh.g3.user.dto.UserResponse;

import java.time.Instant;

/**
 * Datos públicos de una incidencia persistida, expuestos por la API.
 *
 * <p>Incluye el preventivo en el que se detectó la incidencia, el inspector que la comunicó y el
 * técnico asignado cuando exista. No incorpora el histórico de acciones de trabajo, que se
 * reservará para la respuesta de detalle de la incidencia.</p>
 *
 * @param id identificador único de la incidencia
 * @param preventive preventivo en el que se detectó la incidencia
 * @param description descripción del problema detectado
 * @param severity nivel de gravedad como opción de catálogo
 * @param status estado actual de la incidencia como opción de catálogo
 * @param reportedByInspector inspector que comunicó la incidencia
 * @param assignedTechnician técnico responsable; puede ser {@code null} mientras no se asigne
 * @param assignedAt fecha de asignación del técnico; puede ser {@code null}
 * @param startedAt fecha de inicio de la reparación; puede ser {@code null}
 * @param resolvedAt fecha de resolución comunicada por el técnico; puede ser {@code null}
 * @param closedAt fecha de cierre definitivo; puede ser {@code null}
 * @param createdAt fecha de creación de la incidencia
 * @param updatedAt fecha de última actualización de la incidencia
 */
public record IncidentResponse(
    long id,
    PreventiveResponse preventive,
    String description,
    CatalogOptionResponse severity,
    CatalogOptionResponse status,
    UserResponse reportedByInspector,
    UserResponse assignedTechnician,
    Instant assignedAt,
    Instant startedAt,
    Instant resolvedAt,
    Instant closedAt,
    Instant createdAt,
    Instant updatedAt
) {
}
