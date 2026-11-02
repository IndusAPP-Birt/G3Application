package eus.birtlh.g3.common.dto;

import java.time.Instant;
import java.util.Objects;

/**
 * Contenedor estándar para respuestas exitosas de la API.
 *
 * <p>En las respuestas con cuerpo, {@code data} y {@code timestamp} son obligatorios, mientras
 * que {@code message} es opcional. Se recomienda usar {@link #success(Object, String)} para que
 * el servidor asigne la marca temporal.</p>
 *
 * @param <T> tipo de los datos devueltos por la API
 * @param data datos de la respuesta; no puede ser {@code null}
 * @param message mensaje informativo opcional
 * @param timestamp momento de creación de la respuesta; no puede ser {@code null}
 */
public record ApiResponse<T>(
        T data,
        String message,
        Instant timestamp
) {
    /**
     * Valida los campos obligatorios de la respuesta.
     *
     * @throws NullPointerException si {@code data} o {@code timestamp} son {@code null}
     */
    public ApiResponse {
        Objects.requireNonNull(data, "'data' no puede ser null");
        Objects.requireNonNull(timestamp, "'timestamp' no puede ser null");
    }

    /**
     * Crea una respuesta exitosa con la marca temporal actual del servidor.
     *
     * @param data datos de la respuesta; no puede ser {@code null}
     * @param message mensaje informativo opcional
     * @param <T> tipo de los datos devueltos por la API
     * @return respuesta con los datos, el mensaje y la marca temporal actual
     * @throws NullPointerException si {@code data} es {@code null}
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(data, message, Instant.now());
    }
}
