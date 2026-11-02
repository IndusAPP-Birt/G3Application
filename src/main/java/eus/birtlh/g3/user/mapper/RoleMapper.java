package eus.birtlh.g3.user.mapper;

import eus.birtlh.g3.common.dto.CatalogOptionResponse;
import eus.birtlh.g3.user.Role;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Convierte los roles internos del dominio en opciones de catálogo para la API.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RoleMapper {

    /**
     * Transforma un rol en su código y descripción legible.
     *
     * @param role rol que se desea exponer
     * @return opción de catálogo correspondiente al rol
     * @throws IllegalArgumentException si el rol es {@code null}
     */
    public static CatalogOptionResponse toResponse(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("El rol es obligatorio.");
        }

        String description = switch (role) {
            case ROLE_ADMINISTRATOR -> "Administrador";
            case ROLE_INSPECTOR -> "Inspector";
            case ROLE_MAINTENANCE_TECHNICIAN -> "Técnico de mantenimiento";
        };

        return new CatalogOptionResponse(role.name(), description);
    }
}
