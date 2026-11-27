package cl.lacasajojo.common.interno;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Las rutas /internal/** son solo para que los microservicios se llamen entre sí.
 * El gateway no las publica y además exigen el header X-Internal-Key.
 */
@Component
@Order(1)
public class InternoFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Internal-Key";

    private final byte[] clave;

    public InternoFilter(@Value("${app.interno.clave}") String clave) {
        this.clave = clave.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/internal/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String recibida = req.getHeader(HEADER);
        if (recibida == null || !MessageDigest.isEqual(clave, recibida.getBytes(StandardCharsets.UTF_8))) {
            res.setStatus(403);
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write("{\"status\":403,\"mensaje\":\"Ruta interna\",\"errores\":{},\"timestamp\":"
                    + System.currentTimeMillis() + "}");
            return;
        }
        chain.doFilter(req, res);
    }
}
