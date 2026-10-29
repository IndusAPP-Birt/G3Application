package eus.birtlh.g3.common.exception;

import eus.birtlh.g3.common.dto.ApiResponse;
import eus.birtlh.g3.user.exception.EmailAlreadyInUseException;
import eus.birtlh.g3.user.exception.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

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
     * Devuelve una respuesta cuando el cuerpo de la solicitud no puede deserializarse.
     *
     * @return respuesta con estado HTTP 400
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(
    ) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new ApiResponse<>(
                        null,
                        "El cuerpo de la solicitud no tiene un formato válido.",
                        Instant.now()
                )
        );
    }

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
     * Devuelve los errores de validación de parámetros de una solicitud.
     *
     * @param ex excepción que contiene los errores de los parámetros validados
     * @return respuesta con estado HTTP 400 y los errores agrupados por parámetro
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiResponse<Map<String, List<String>>>> handleHandlerMethodValidationException(HandlerMethodValidationException ex) {

        Map<String, List<String>> errors = new LinkedHashMap<>();

        ex.getParameterValidationResults().forEach(result -> {
            String parameter = result.getMethodParameter().getParameterName();

            result.getResolvableErrors().forEach(error ->
                    errors.computeIfAbsent(parameter, key -> new ArrayList<>())
                            .add(error.getDefaultMessage())
            );
        });

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

    /**
     * Devuelve una respuesta cuando no se encuentra el usuario solicitado.
     *
     * @param ex excepción producida al no encontrar el usuario
     * @return respuesta con estado HTTP 404
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserNotFoundException(
            UserNotFoundException ex) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ApiResponse<>(
                        null,
                        ex.getMessage(),
                        Instant.now()
                )
        );
    }

    // TODO: Gestionar DataIntegrityViolationException provocada por violaciones del índice único
    //  de email y devolver HTTP 409 en solicitudes concurrentes.
    /**
     * Devuelve una respuesta cuando la dirección de correo electrónico ya está asignada.
     *
     * @param ex excepción producida por la duplicidad de la dirección de correo electrónico
     * @return respuesta con estado HTTP 409
     */
    @ExceptionHandler(EmailAlreadyInUseException.class)
    public ResponseEntity<ApiResponse<Void>> handleEmailAlreadyInUseException(EmailAlreadyInUseException ex) {

        return ResponseEntity.status(HttpStatus.CONFLICT).body(
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
