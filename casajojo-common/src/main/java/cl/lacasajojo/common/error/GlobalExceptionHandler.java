package cl.lacasajojo.common.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

/** Convierte cualquier excepción en un ApiError JSON con el código HTTP correcto. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> negocio(ApiException e) {
        return responder(e.getStatus(), e.getMessage(), e.getErrores());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validacion(MethodArgumentNotValidException e) {
        Map<String, String> errores = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(f -> errores.putIfAbsent(f.getField(), f.getDefaultMessage()));
        return responder(HttpStatus.BAD_REQUEST, errores.values().stream().findFirst().orElse("Datos inválidos"), errores);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class})
    public ResponseEntity<ApiError> formato(Exception e) {
        return responder(HttpStatus.BAD_REQUEST, "El formato de la solicitud no es válido", Map.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> integridad(DataIntegrityViolationException e) {
        return responder(HttpStatus.CONFLICT, "La operación viola una restricción de datos (registro duplicado o en uso)", Map.of());
    }

    /** Otro microservicio no respondió (está apagado o caído). */
    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ApiError> servicioCaido(ResourceAccessException e) {
        log.warn("Microservicio no disponible: {}", e.getMessage());
        return responder(HttpStatus.SERVICE_UNAVAILABLE, "Un servicio interno no está disponible. Intenta en unos segundos.", Map.of());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> rutaNoExiste(NoResourceFoundException e) {
        return responder(HttpStatus.NOT_FOUND, "Ruta no encontrada", Map.of());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> metodo(HttpRequestMethodNotSupportedException e) {
        return responder(HttpStatus.METHOD_NOT_ALLOWED, "Método HTTP no permitido en esta ruta", Map.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> inesperado(Exception e) {
        log.error("Error inesperado", e);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado. Intenta nuevamente.", Map.of());
    }

    private ResponseEntity<ApiError> responder(HttpStatus status, String mensaje, Map<String, String> errores) {
        return ResponseEntity.status(status).body(ApiError.de(status.value(), mensaje, errores));
    }
}
