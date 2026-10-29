package eus.birtlh.g3.common.exception;

import eus.birtlh.g3.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

/**
 * Gestiona de forma centralizada las excepciones producidas por los controladores REST.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Gestiona solicitudes con datos que no cumplen las reglas de dominio.
     *
     * @param ex excepción de argumento inválido
     * @return respuesta HTTP 400 con el detalle de la validación
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new ApiResponse<>(
                  null,
                  ex.getMessage(),
                  Instant.now()
                )
        );
    }

    // TODO: Cuando sea necesario distinguir conflictos de dominio, sustituir IllegalStateException
    //  por excepciones específicas y devolver HTTP 409 para cada conflicto de estado.
    /**
     * Gestiona solicitudes incompatibles con el estado actual del recurso.
     *
     * @param ex excepción de estado inválido
     * @return respuesta HTTP 400 con el detalle del conflicto
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalStateException(IllegalStateException ex) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new ApiResponse<>(
                        null,
                        ex.getMessage(),
                        Instant.now()
                )
        );
    }

    /**
     * Registra y oculta los detalles de cualquier excepción no controlada.
     *
     * @param ex excepción no controlada
     * @return respuesta genérica con estado HTTP 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception ex) {

        log.error("Error no controlado.", ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ApiResponse<>(
                        null,
                        "Se ha producido un error interno en el servidor.",
                        Instant.now()
                )
        );
    }
}
