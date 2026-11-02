package eus.birtlh.g3.common.exception;

import eus.birtlh.g3.common.dto.ApiErrorResponse;
import eus.birtlh.g3.user.exception.EmailAlreadyInUseException;
import eus.birtlh.g3.user.exception.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Traduce las excepciones de la aplicación en respuestas de error HTTP homogéneas.
 *
 * <p>Las excepciones no previstas se registran en el servidor y se exponen con un mensaje
 * genérico, sin revelar detalles internos.</p>
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Devuelve una respuesta HTTP 404 cuando no existe el usuario solicitado.
     *
     * @param ex excepción que identifica el usuario no encontrado
     * @return respuesta de error con estado HTTP 404
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleUserNotFoundException(
            UserNotFoundException ex
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.error(ex.getMessage()));
    }

    /**
     * Devuelve una respuesta HTTP 409 cuando el correo electrónico ya está en uso.
     *
     * @param ex excepción que identifica el conflicto de correo electrónico
     * @return respuesta de error con estado HTTP 409
     */
    @ExceptionHandler(EmailAlreadyInUseException.class)
    public ResponseEntity<ApiErrorResponse> handleEmailAlreadyInUseException(
            EmailAlreadyInUseException ex
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiErrorResponse.error(ex.getMessage()));
    }

    /**
     * Devuelve una respuesta HTTP 400 con los errores de validación de un cuerpo de solicitud.
     *
     * @param ex excepción que contiene los errores de validación del DTO recibido
     * @return respuesta de error con estado HTTP 400 y mensajes agrupados por campo
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex
    ) {
        Map<String, List<String>> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.groupingBy(
                        FieldError::getField,
                        Collectors.mapping(
                                FieldError::getDefaultMessage,
                                Collectors.toList()
                        )
                ));

        return ResponseEntity.badRequest()
                .body(ApiErrorResponse.validation(
                        "La solicitud contiene datos inválidos.",
                        errors
                ));
    }

    /**
     * Devuelve una respuesta HTTP 400 con los errores de validación de parámetros del método.
     *
     * @param ex excepción que contiene los errores de parámetros como ruta o consulta
     * @return respuesta de error con estado HTTP 400 y mensajes agrupados por parámetro
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleHandlerMethodValidationException(
            HandlerMethodValidationException ex
    ) {
        Map<String, List<String>> errors = new LinkedHashMap<>();

        ex.getParameterValidationResults().forEach(result -> {
            String parameter = result.getMethodParameter().getParameterName();

            result.getResolvableErrors().forEach(error ->
                    errors.computeIfAbsent(parameter, key -> new ArrayList<>())
                            .add(error.getDefaultMessage())
            );
        });

        return ResponseEntity.badRequest()
                .body(ApiErrorResponse.validation(
                        "La solicitud contiene parámetros inválidos.",
                        errors
                ));
    }

    /**
     * Devuelve una respuesta HTTP 400 cuando un parámetro no puede convertirse al tipo requerido.
     *
     * @param ex excepción que identifica el parámetro con formato incompatible
     * @return respuesta de error con estado HTTP 400
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex
    ) {
        return ResponseEntity.badRequest()
                .body(ApiErrorResponse.error(
                        "El parámetro '" + ex.getName() + "' tiene un formato inválido."
                ));
    }

    /**
     * Devuelve una respuesta HTTP 400 cuando el cuerpo de la solicitud no puede deserializarse.
     *
     * @param ex excepción causada por JSON inválido o por valores incompatibles
     * @return respuesta de error con estado HTTP 400
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex
    ) {
        return ResponseEntity.badRequest()
                .body(ApiErrorResponse.error(
                        "El cuerpo de la solicitud no tiene un formato válido."
                ));
    }

    /**
     * Registra una excepción no prevista y devuelve una respuesta HTTP 500 sin detalles internos.
     *
     * <p>También trata {@link IllegalArgumentException} originadas por invariantes internas. No
     * se traducen a HTTP 400, porque no representan errores de entrada del cliente.</p>
     *
     * <p>La {@link org.springframework.dao.DataIntegrityViolationException} no tiene un
     * tratamiento específico por decisión de alcance. La base de datos preserva la integridad y,
     * si ocurre una carrera concurrente, se devuelve esta respuesta HTTP 500 genérica.</p>
     *
     * <p>Los {@link NullPointerException} derivados de contratos internos, como construir una
     * respuesta sin campos obligatorios, también se registran y devuelven como HTTP 500.</p>
     *
     * @param ex excepción no controlada durante la petición
     * @return respuesta de error genérica con estado HTTP 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleException(
            Exception ex
    ) {
        log.error("Error no controlado durante la petición.", ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorResponse.error("Error interno del servidor."));
    }
}
