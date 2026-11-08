package eus.birtlh.g3.common.exception;

import eus.birtlh.g3.common.dto.ApiErrorResponse;
import eus.birtlh.g3.machine.exception.AssetCodeAlreadyInUseException;
import eus.birtlh.g3.machine.exception.MachineNotFoundException;
import eus.birtlh.g3.machine.exception.SerialNumberAlreadyInUseException;
import eus.birtlh.g3.preventive.exceptions.PreventivePlanNotFoundException;
import eus.birtlh.g3.user.exception.EmailAlreadyInUseException;
import eus.birtlh.g3.user.exception.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
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
     * Devuelve una respuesta HTTP 404 cuando no existe la máquina solicitada.
     *
     * @param ex excepción que identifica la máquina no encontrada
     * @return respuesta de error con estado HTTP 404
     */
    @ExceptionHandler(MachineNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleMachineNotFoundException(
            MachineNotFoundException ex
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.error(ex.getMessage()));
    }

    /**
     * Devuelve una respuesta HTTP 404 cuando no existe el plan preventivo solicitado.
     *
     * @param ex excepción que identifica el plan preventivo no encontrado
     * @return respuesta de error con estado HTTP 404
     */
    @ExceptionHandler(PreventivePlanNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handlePreventivePlanNotFoundException(
            PreventivePlanNotFoundException ex
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
     * Devuelve una respuesta HTTP 409 cuando el código patrimonial ya está en uso.
     *
     * @param ex excepción que identifica el conflicto de código patrimonial
     * @return respuesta de error con estado HTTP 409
     */
    @ExceptionHandler(AssetCodeAlreadyInUseException.class)
    public ResponseEntity<ApiErrorResponse> handleAssetCodeAlreadyInUseException(
            AssetCodeAlreadyInUseException ex
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiErrorResponse.error(ex.getMessage()));
    }

    /**
     * Devuelve una respuesta HTTP 409 cuando el número de serie ya está en uso.
     *
     * @param ex excepción que identifica el conflicto de número de serie
     * @return respuesta de error con estado HTTP 409
     */
    @ExceptionHandler(SerialNumberAlreadyInUseException.class)
    public ResponseEntity<ApiErrorResponse> handleSerialNumberAlreadyInUseException(
            SerialNumberAlreadyInUseException ex
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
     * Devuelve una respuesta HTTP 400 cuando falta un parámetro obligatorio de la solicitud.
     *
     * @param ex excepción que identifica el parámetro de consulta o formulario no recibido
     * @return respuesta de error con estado HTTP 400 y un mensaje asociado al parámetro ausente
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex
    ) {
        return ResponseEntity.badRequest()
                .body(ApiErrorResponse.validation(
                        "La solicitud contiene parámetros inválidos.",
                        Map.of(
                                ex.getParameterName(),
                                List.of("El parámetro es obligatorio.")
                        )
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
     * Devuelve una respuesta HTTP 405 cuando la ruta no admite el método HTTP solicitado.
     *
     * <p>Conserva la cabecera {@code Allow} con los métodos admitidos para el recurso.</p>
     *
     * @param ex excepción que identifica el método no admitido y sus métodos alternativos
     * @return respuesta de error con estado HTTP 405 y cabecera {@code Allow}
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex
    ) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .headers(ex.getHeaders())
                .body(ApiErrorResponse.error(
                        "El método HTTP solicitado no está permitido para este recurso."
                ));
    }

    /**
     * Devuelve una respuesta HTTP 415 cuando el tipo de contenido de la solicitud no es admitido.
     *
     * <p>Conserva las cabeceras con los tipos de contenido admitidos proporcionadas por Spring.</p>
     *
     * @param ex excepción que identifica el tipo de contenido no admitido
     * @return respuesta de error con estado HTTP 415 y los tipos admitidos
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMediaTypeNotSupportedException(
            HttpMediaTypeNotSupportedException ex
    ) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .headers(ex.getHeaders())
                .body(ApiErrorResponse.error(
                        "El tipo de contenido debe ser application/json."
                ));
    }

    /**
     * Devuelve una respuesta HTTP 406 cuando el cliente solicita un formato no disponible.
     *
     * <p>Conserva las cabeceras con las representaciones admitidas proporcionadas por Spring.</p>
     *
     * @param ex excepción que identifica una cabecera {@code Accept} incompatible
     * @return respuesta de error con estado HTTP 406 y las representaciones admitidas
     */
    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMediaTypeNotAcceptableException(
            HttpMediaTypeNotAcceptableException ex
    ) {
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                .headers(ex.getHeaders())
                .body(ApiErrorResponse.error(
                        "La aplicación no puede devolver una respuesta en el formato solicitado."
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
