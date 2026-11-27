package cl.lacasajojo.common.seguridad;

import cl.lacasajojo.common.error.ApiError;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Lee el header "Authorization: Bearer <token>". Si es válido deja el usuario en la petición
 * (ver {@link Seguridad}); si viene un token inválido o vencido responde 401. Sin token, la petición
 * sigue como anónima y cada endpoint decide si lo exige.
 */
@Component
@Order(2)
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AntPathMatcher matcher = new AntPathMatcher();

    /** Rutas que exigen token sí o sí (responde 401 antes de validar el cuerpo). */
    @Value("${app.seguridad.rutas-protegidas:}")
    private List<String> rutasProtegidas;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String header = req.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            Optional<UsuarioAutenticado> usuario = jwtService.validar(header.substring(7).trim());
            if (usuario.isEmpty()) {
                escribirError(res, 401, "Tu sesión venció o no es válida. Inicia sesión nuevamente.");
                return;
            }
            req.setAttribute(Seguridad.ATRIBUTO, usuario.get());
        } else if (!"OPTIONS".equals(req.getMethod()) && esProtegida(req.getRequestURI())) {
            escribirError(res, 401, "Debes iniciar sesión");
            return;
        }
        chain.doFilter(req, res);
    }

    private boolean esProtegida(String uri) {
        return rutasProtegidas != null && rutasProtegidas.stream()
                .filter(r -> !r.isBlank()).anyMatch(r -> matcher.match(r.trim(), uri));
    }

    static void escribirError(HttpServletResponse res, int status, String mensaje) throws IOException {
        ApiError e = ApiError.de(status, mensaje, null);
        res.setStatus(status);
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write("{\"status\":" + e.status() + ",\"mensaje\":\"" + mensaje.replace("\"", "'")
                + "\",\"errores\":{},\"timestamp\":" + e.timestamp() + "}");
    }
}
