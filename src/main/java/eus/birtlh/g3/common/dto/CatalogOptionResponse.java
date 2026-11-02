package eus.birtlh.g3.common.dto;

/**
 * Representa una opción de catálogo expuesta por la API.
 *
 * <p>Permite devolver un código estable para el cliente junto con su descripción legible, sin
 * exponer directamente el modelo interno que lo origina.</p>
 *
 * @param code código identificador de la opción
 * @param description descripción legible de la opción
 */
public record CatalogOptionResponse(
        String code,
        String description
) {
}
