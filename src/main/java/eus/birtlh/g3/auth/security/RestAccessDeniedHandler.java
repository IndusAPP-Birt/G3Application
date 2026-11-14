package eus.birtlh.g3.auth.security;

import eus.birtlh.g3.common.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Genera la respuesta JSON cuando un usuario autenticado no dispone del permiso requerido.
 *
 * <p>Se ejecuta dentro de la cadena de filtros de Spring Security, antes de llegar al controlador
 * o al {@code GlobalExceptionHandler}.</p>
 */
@Component
@RequiredArgsConstructor
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    /**
     * Devuelve una respuesta HTTP 403 sin revelar detalles internos de autorización.
     *
     * @param request petición HTTP cuyo acceso fue denegado
     * @param response respuesta HTTP que se enviará al cliente
     * @param accessDeniedException excepción de autorización producida por Spring Security
     * @throws IOException si no se puede escribir la respuesta JSON
     */
    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        objectMapper.writeValue(
                response.getOutputStream(),
                ApiErrorResponse.error("No tienes permisos para acceder a este recurso.")
        );
    }
}
