package cl.lacasajojo.common.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Map;

/** Error de negocio con su código HTTP. */
@Getter
public class ApiException extends RuntimeException {
    private final HttpStatus status;
    private final Map<String, String> errores;

    public ApiException(HttpStatus status, String mensaje) { this(status, mensaje, Map.of()); }

    public ApiException(HttpStatus status, String mensaje, Map<String, String> errores) {
        super(mensaje);
        this.status = status;
        this.errores = errores;
    }

    public static ApiException noEncontrado(String m) { return new ApiException(HttpStatus.NOT_FOUND, m); }
    public static ApiException conflicto(String m) { return new ApiException(HttpStatus.CONFLICT, m); }
    public static ApiException noAutenticado(String m) { return new ApiException(HttpStatus.UNAUTHORIZED, m); }
    public static ApiException prohibido(String m) { return new ApiException(HttpStatus.FORBIDDEN, m); }
    public static ApiException servicioNoDisponible(String m) { return new ApiException(HttpStatus.SERVICE_UNAVAILABLE, m); }

    public static ApiException campo(HttpStatus status, String campo, String mensaje) {
        return new ApiException(status, mensaje, Map.of(campo, mensaje));
    }

    public static ApiException campo(String campo, String mensaje) {
        return campo(HttpStatus.BAD_REQUEST, campo, mensaje);
    }
}
