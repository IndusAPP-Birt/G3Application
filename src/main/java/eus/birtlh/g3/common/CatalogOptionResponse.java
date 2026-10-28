package eus.birtlh.g3.common;

/**
 * DTO que representa una opción de un catálogo en las respuestas de la API.
 *
 * @param code código que identifica la opción
 * @param description descripción legible de la opción
 */
public record CatalogOptionResponse(
        String code,
        String description
) {
}
