package eus.birtlh.g3.preventive.mapper;

import eus.birtlh.g3.machine.mapper.MachineMapper;
import eus.birtlh.g3.preventive.PreventivePlanAssignment;
import eus.birtlh.g3.preventive.dto.PreventivePlanAssignmentResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Convierte asignaciones de plantillas preventivas a máquinas en sus DTOs públicos de respuesta.
 *
 * <p>Transforma tanto la plantilla preventiva como la máquina asociada mediante sus mappers
 * públicos correspondientes.</p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PreventivePlanAssignmentMapper {

    /**
     * Transforma una asignación preventiva persistida en su representación pública.
     *
     * @param preventivePlanAssignment asignación preventiva que se desea exponer
     * @return DTO público correspondiente a la asignación
     * @throws IllegalArgumentException si la asignación es {@code null} o no está persistida
     */
    public static PreventivePlanAssignmentResponse toResponse(PreventivePlanAssignment preventivePlanAssignment) {
        if (preventivePlanAssignment == null) {
            throw new IllegalArgumentException("La asignación del plan preventivo es obligatoria.");
        }

        if (preventivePlanAssignment.getId() == null) {
            throw new IllegalArgumentException("La asignación del plan preventivo debe estar persistida.");
        }

        return new PreventivePlanAssignmentResponse(
                preventivePlanAssignment.getId(),
                PreventivePlanMapper.toResponse(preventivePlanAssignment.getPreventivePlan()),
                MachineMapper.toResponse(preventivePlanAssignment.getMachine()),
                preventivePlanAssignment.getIntervalDays(),
                preventivePlanAssignment.getNoticeDays(),
                preventivePlanAssignment.getNextDueDate(),
                preventivePlanAssignment.isActive(),
                preventivePlanAssignment.getCreatedAt(),
                preventivePlanAssignment.getUpdatedAt()
        );
    }
}
