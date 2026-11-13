package eus.birtlh.g3.common.dto;

import java.util.List;

/**
 * Representa una página de resultados expuesta por la API.
 *
 * @param <T> tipo de los elementos de la página
 * @param content elementos de la página actual; puede estar vacía
 * @param page índice de la página actual, basado en cero
 * @param size tamaño solicitado para cada página
 * @param totalElements número total de elementos disponibles
 * @param totalPages número total de páginas disponibles
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
