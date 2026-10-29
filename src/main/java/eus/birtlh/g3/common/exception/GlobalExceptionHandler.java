package eus.birtlh.g3.common.exception;

import eus.birtlh.g3.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Gestiona de forma centralizada las excepciones producidas por los controladores REST.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Devuelve los errores de validación de un cuerpo de solicitud.
     *
     * @param ex excepción que contiene los errores de los campos validados
     * @return respuesta con estado HTTP 400 y los errores agrupados por campo
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, List<String>>>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {

        Map<String, List<String>> errors = new LinkedHashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.computeIfAbsent(
                        error.getField(),
                        key -> new ArrayList<>()
                ).add(error.getDefaultMessage())
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new ApiResponse<>(
                        errors,
                        "La solicitud contiene errores de validación.",
                        Instant.now()
                )
        );
    }

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
