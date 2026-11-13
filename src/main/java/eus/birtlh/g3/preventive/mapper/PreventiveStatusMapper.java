package eus.birtlh.g3.preventive.mapper;

import eus.birtlh.g3.common.dto.CatalogOptionResponse;
import eus.birtlh.g3.preventive.PreventiveStatus;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Convierte estados de ejecución preventiva en opciones legibles de catálogo.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PreventiveStatusMapper {

    /**
     * Transforma un estado preventivo en su código y descripción legible.
     *
     * @param status estado que se desea exponer
     * @return opción de catálogo correspondiente al estado
     * @throws IllegalArgumentException si el estado es {@code null}
     */
    public static CatalogOptionResponse toResponse(PreventiveStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("El estado del preventivo es obligatorio.");
        }

        String description = switch (status) {
            case PENDING -> "Pendiente de asignación";
            case ASSIGNED -> "Asignado";
            case IN_PROGRESS -> "En progreso";
            case COMPLETED -> "Completado";
        };

        return new CatalogOptionResponse(status.name(), description);
    }
}
