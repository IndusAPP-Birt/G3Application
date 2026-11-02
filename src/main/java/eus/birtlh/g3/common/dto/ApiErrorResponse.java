package eus.birtlh.g3.common.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Contenedor estándar para respuestas de error de la API.
 *
 * <p>Incluye un mensaje general, detalles opcionales por campo y una marca temporal asignada por
 * el servidor. Para errores no asociados a campos, {@code errors} es un mapa vacío.</p>
 *
 * @param message descripción general del error; no puede ser {@code null}
 * @param errors mensajes de error agrupados por campo; no puede ser {@code null}
 * @param timestamp momento de creación de la respuesta; no puede ser {@code null}
 */
public record ApiErrorResponse(
        String message,
        Map<String, List<String>> errors,
        Instant timestamp
) {
    /**
     * Valida los campos obligatorios y realiza una copia defensiva profunda de los errores.
     *
     * @throws NullPointerException si {@code message}, {@code errors} o {@code timestamp} son
     * {@code null}, si el mapa contiene claves o valores nulos, o si alguna lista contiene
     * elementos nulos
     */
    public ApiErrorResponse {
        Objects.requireNonNull(message, "'message' no puede ser null");
        Objects.requireNonNull(errors, "'errors' no puede ser null");
        Objects.requireNonNull(timestamp, "'timestamp' no puede ser null");

        errors = errors.entrySet()
                .stream()
                .collect(Collectors.toUnmodifiableMap(
                        Map.Entry::getKey,
                        entry -> List.copyOf(entry.getValue())
                ));
    }

    /**
     * Crea una respuesta de error general sin detalles asociados a campos.
     *
     * @param message descripción general del error; no puede ser {@code null}
     * @return respuesta de error con una marca temporal asignada por el servidor
     * @throws NullPointerException si {@code message} es {@code null}
     */
    public static ApiErrorResponse error(String message) {
        return new ApiErrorResponse(message, Map.of(), Instant.now());
    }

    /**
     * Crea una respuesta de error de validación con detalles agrupados por campo.
     *
     * @param message descripción general del error; no puede ser {@code null}
     * @param errors mensajes de error agrupados por campo; no puede ser {@code null}
     * @return respuesta de error con detalles de validación y una marca temporal del servidor
     * @throws NullPointerException si alguno de los parámetros es {@code null}, o si el mapa
     * contiene claves o valores nulos, o si alguna lista contiene elementos nulos
     */
    public static ApiErrorResponse validation(
            String message,
            Map<String, List<String>> errors
    ) {
        return new ApiErrorResponse(message, errors, Instant.now());
    }
}
