package eus.birtlh.g3.user.dto;

import eus.birtlh.g3.common.dto.CatalogOptionResponse;

import java.time.Instant;

/**
 * DTO que representa los datos de un usuario expuestos en las respuestas de la API.
 *
 * @param id identificador del usuario
 * @param name nombre del usuario
 * @param lastName apellidos del usuario; pueden ser {@code null}
 * @param email dirección de correo electrónico del usuario
 * @param role opción de catálogo que representa el rol asignado al usuario
 * @param enabled indica si el usuario está habilitado
 * @param createdAt fecha y hora de creación del usuario
 * @param updatedAt fecha y hora de la última actualización del usuario
 */
public record UserResponse(
        Long id,
        String name,
        String lastName,
        String email,
        CatalogOptionResponse role,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {
}
