package eus.birtlh.g3.preventive.mapper;

import eus.birtlh.g3.machine.mapper.MachineMapper;
import eus.birtlh.g3.preventive.PreventivePlan;
import eus.birtlh.g3.preventive.dto.PreventivePlanResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Convierte entidades {@link PreventivePlan} persistidas en sus DTOs públicos de respuesta.
 *
 * <p>La máquina asociada se transforma mediante {@link MachineMapper} para incluir su
 * representación pública completa en la respuesta del plan.</p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PreventivePlanMapper {

    /**
     * Transforma un plan preventivo persistido en su representación pública.
     *
     * @param plan plan preventivo que se desea exponer
     * @return DTO público correspondiente al plan preventivo
     * @throws IllegalArgumentException si el plan es {@code null}, no está persistido o no tiene
     *                                  una máquina válida
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
                MachineMapper.toResponse(plan.getMachine()),
                plan.getName(),
                plan.getDescription(),
                plan.getIntervalDays(),
                plan.getNoticeDays(),
                plan.getNextDueDate(),
                plan.isActive(),
                plan.getCreatedAt(),
                plan.getUpdatedAt()
        );
    }
}
