package eus.birtlh.g3.common;

import java.time.Instant;

/**
 * DTO genérico que estructura las respuestas HTTP de la API.
 *
 * @param <T> tipo de dato devuelto en la respuesta
 */
@SuppressWarnings("SpellCheckingInspection")
public record ApiResponse<T>(
        T data,
        String message,
        Instant timestamp
) {
}
