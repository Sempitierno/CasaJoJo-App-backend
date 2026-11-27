package cl.lacasajojo.common.error;

import java.util.Map;

/** Formato único de error de todos los microservicios. "errores" trae un mensaje por campo. */
public record ApiError(int status, String mensaje, Map<String, String> errores, long timestamp) {
    public static ApiError de(int status, String mensaje, Map<String, String> errores) {
        return new ApiError(status, mensaje, errores == null ? Map.of() : errores, System.currentTimeMillis());
    }
}
