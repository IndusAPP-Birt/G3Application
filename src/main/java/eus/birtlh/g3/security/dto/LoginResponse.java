package eus.birtlh.g3.security.dto;

/**
 * DTO que representa el resultado de una autenticación correcta.
 *
 * @param token token de acceso JWT
 * @param tokenType tipo de token usado en la cabecera de autorización
 * @param expiresIn tiempo de expiración del token en segundos
 */
public record LoginResponse(
        String token,
        String tokenType,
        long expiresIn
) {
}
