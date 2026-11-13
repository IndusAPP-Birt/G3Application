package eus.birtlh.g3.preventive.mapper;

import eus.birtlh.g3.preventive.PreventivePlan;
import eus.birtlh.g3.preventive.dto.PreventivePlanResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Convierte plantillas {@link PreventivePlan} persistidas en sus DTOs públicos de respuesta.
 *
 * <p>La plantilla no incluye información de activos ni de planificación, que pertenecen a sus
 * asignaciones específicas.</p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PreventivePlanMapper {

    /**
     * Transforma una plantilla preventiva persistida en su representación pública.
     *
     * @param plan plantilla preventiva que se desea exponer
     * @return DTO público correspondiente a la plantilla
     * @throws IllegalArgumentException si la plantilla es {@code null} o no está persistida
     */
    public static PreventivePlanResponse toResponse(PreventivePlan plan) {
        if (plan == null) {
            throw new IllegalArgumentException("El plan preventivo es obligatorio.");
        }

        if (plan.getId() == null) {
            throw new IllegalArgumentException("El plan preventivo debe estar persistido.");
        }

        return new PreventivePlanResponse(
                plan.getId(),
                plan.getName(),
                plan.getDescription(),
                plan.isActive(),
                plan.getCreatedAt(),
                plan.getUpdatedAt()
        );
    }
}
