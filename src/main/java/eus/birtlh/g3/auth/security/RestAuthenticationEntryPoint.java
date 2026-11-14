package eus.birtlh.g3.auth.security;

import eus.birtlh.g3.common.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Genera la respuesta JSON para peticiones que no incluyen un JWT válido.
 *
 * <p>Se ejecuta dentro de la cadena de filtros de Spring Security, antes de llegar al controlador
 * o al {@code GlobalExceptionHandler}.</p>
 */
@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    /**
     * Devuelve una respuesta HTTP 401 sin revelar la causa concreta del rechazo del token.
     *
     * @param request petición HTTP que no pudo autenticarse
     * @param response respuesta HTTP que se enviará al cliente
     * @param authenticationException excepción de autenticación producida por Spring Security
     * @throws IOException si no se puede escribir la respuesta JSON
     */
    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authenticationException
    ) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        objectMapper.writeValue(
                response.getOutputStream(),
                ApiErrorResponse.error("No se ha proporcionado un token de acceso válido.")
        );
    }
}
