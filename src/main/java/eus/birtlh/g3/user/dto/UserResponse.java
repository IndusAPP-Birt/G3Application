package eus.birtlh.g3.user.dto;

import eus.birtlh.g3.common.dto.CatalogOptionResponse;

import java.time.Instant;

/**
 * Datos públicos de un usuario persistido, expuestos por la API.
 *
 * <p>No incluye información sensible, como el hash de la contraseña. El rol se representa como
 * una opción de catálogo para no exponer directamente el modelo interno.</p>
 *
 * @param id identificador único del usuario
 * @param name nombre del usuario
 * @param lastName apellido del usuario; puede ser {@code null}
 * @param email dirección de correo electrónico del usuario
 * @param role rol asignado al usuario
 * @param enabled indica si el usuario puede acceder al sistema
 * @param createdAt fecha de creación del usuario
 * @param updatedAt fecha de última actualización del usuario
 */
public record UserResponse(
        long id,
        String name,
        String lastName,
        String email,
        CatalogOptionResponse role,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {
}
