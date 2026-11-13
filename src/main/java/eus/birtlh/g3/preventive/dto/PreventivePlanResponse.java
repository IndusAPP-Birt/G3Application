package eus.birtlh.g3.preventive.dto;

import java.time.Instant;

/**
 * Datos públicos de una plantilla de plan preventivo, expuestos por la API.
 *
 * <p>La plantilla es independiente de los activos. Sus asignaciones a máquinas y la
 * planificación asociada se exponen mediante recursos específicos.</p>
 *
 * @param id identificador único del plan preventivo
 * @param name nombre descriptivo de la plantilla
 * @param description acciones preventivas o instrucciones; puede ser {@code null}
 * @param active indica si la plantilla está disponible para nuevas asignaciones
 * @param createdAt fecha de creación del plan
 * @param updatedAt fecha de última actualización del plan
 */
public record PreventivePlanResponse(
        long id,
        String name,
        String description,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
