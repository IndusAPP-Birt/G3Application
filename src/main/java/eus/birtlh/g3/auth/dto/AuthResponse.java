package eus.birtlh.g3.auth.dto;

/**
 * Datos devueltos tras una autenticación correcta.
 *
 * @param accessToken token JWT de acceso
 * @param tokenType tipo de token utilizado en la cabecera de autorización, normalmente
 * {@code Bearer}
 * @param expiresIn duración del token en segundos
 */
public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {
}
