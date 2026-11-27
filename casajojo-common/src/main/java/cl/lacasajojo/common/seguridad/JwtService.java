package cl.lacasajojo.common.seguridad;

import cl.lacasajojo.common.modelo.Rol;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

/**
 * Genera y valida tokens JWT firmados con HMAC-SHA256. Todos los microservicios comparten la misma
 * clave (variable de entorno JWT_SECRET), así cada uno valida el token sin llamar a ms-usuarios.
 */
@Component
public class JwtService {

    private final SecretKey clave;
    private final long duracionMs;

    public JwtService(@Value("${app.jwt.secret}") String secreto,
                      @Value("${app.jwt.duracion-horas:8}") long duracionHoras) {
        byte[] bytes = secreto.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("app.jwt.secret debe tener al menos 32 caracteres");
        }
        this.clave = Keys.hmacShaKeyFor(bytes);
        this.duracionMs = duracionHoras * 3_600_000L;
    }

    public String generar(UsuarioAutenticado u) {
        long ahora = System.currentTimeMillis();
        return Jwts.builder()
                .subject(String.valueOf(u.id()))
                .claim("correo", u.correo())
                .claim("rol", u.rol().name())
                .issuedAt(new Date(ahora))
                .expiration(new Date(ahora + duracionMs))
                .signWith(clave)
                .compact();
    }

    public long getDuracionMs() {
        return duracionMs;
    }

    /** Devuelve el usuario si el token es válido y no ha vencido; vacío en cualquier otro caso. */
    public Optional<UsuarioAutenticado> validar(String token) {
        try {
            Claims c = Jwts.parser().verifyWith(clave).build().parseSignedClaims(token).getPayload();
            return Optional.of(new UsuarioAutenticado(Long.valueOf(c.getSubject()),
                    c.get("correo", String.class), Rol.valueOf(c.get("rol", String.class))));
        } catch (JwtException | IllegalArgumentException | NullPointerException e) {
            return Optional.empty();
        }
    }
}
