package eus.birtlh.g3.common.dto;

import java.time.Instant;

/**
 * DTO genérico que estructura las respuestas HTTP de la API.
 *
 * @param <T> tipo de dato devuelto en la respuesta
 * @param data datos devueltos por la operación
 * @param message mensaje descriptivo del resultado de la operación
 * @param timestamp fecha y hora en que se genera la respuesta
 */
@SuppressWarnings("SpellCheckingInspection")
public record ApiResponse<T>(
        T data,
        String message,
        Instant timestamp
) {
}
