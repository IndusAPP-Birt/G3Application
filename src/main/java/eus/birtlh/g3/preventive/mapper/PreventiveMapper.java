package eus.birtlh.g3.preventive.mapper;

import eus.birtlh.g3.preventive.Preventive;
import eus.birtlh.g3.preventive.dto.PreventiveResponse;
import eus.birtlh.g3.user.mapper.UserMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Convierte entidades {@link Preventive} persistidas en sus DTOs públicos de respuesta.
 *
 * <p>El plan de origen se transforma mediante {@link PreventivePlanMapper}. Cuando todavía no
 * se ha asignado un inspector, la respuesta conserva ese valor como {@code null}.</p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PreventiveMapper {

    /**
     * Transforma un preventivo persistido en su representación pública.
     *
     * @param preventive preventivo que se desea exponer
     * @return DTO público correspondiente al preventivo
     * @throws IllegalArgumentException si el preventivo es {@code null}, no está persistido o no
     *                                  tiene un plan de origen válido
     */
    public static PreventiveResponse toResponse(Preventive preventive) {
        if (preventive == null) {
            throw new IllegalArgumentException("El preventivo es obligatorio.");
        }

        if (preventive.getId() == null) {
            throw new IllegalArgumentException("El preventivo debe estar persistido.");
        }

        return new PreventiveResponse(
                preventive.getId(),
                PreventivePlanMapper.toResponse(preventive.getPlan()),
                preventive.getAssignedInspector() == null
                ? null : UserMapper.toResponse(preventive.getAssignedInspector()),
                preventive.getDueDate(),
                preventive.getStatus(),
                preventive.getWorkPerformed(),
                preventive.getAssignedAt(),
                preventive.getStartedAt(),
                preventive.getCompletedAt(),
                preventive.getCreatedAt(),
                preventive.getUpdatedAt()
        );
    }
}
