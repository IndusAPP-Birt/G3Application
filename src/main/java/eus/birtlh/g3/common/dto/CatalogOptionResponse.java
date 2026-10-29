package eus.birtlh.g3.common.dto;

/**
 * DTO que representa una opción de un catálogo en las respuestas de la API.
 *
 * @param code código único que identifica la opción dentro del catálogo
 * @param description descripción legible de la opción
 */
public record CatalogOptionResponse(
        String code,
        String description
) {
}
