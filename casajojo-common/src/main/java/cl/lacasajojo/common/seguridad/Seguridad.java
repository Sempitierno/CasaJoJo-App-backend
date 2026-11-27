package cl.lacasajojo.common.seguridad;

import cl.lacasajojo.common.error.ApiException;
import cl.lacasajojo.common.modelo.Rol;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Arrays;
import java.util.Optional;

/** Ayudas para que los controladores exijan sesión y roles: Seguridad.requerirRol(Rol.ADMINISTRADOR). */
public final class Seguridad {

    public static final String ATRIBUTO = "casajojo.usuario";

    private Seguridad() {}

    public static Optional<UsuarioAutenticado> actualOpcional() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs == null) return Optional.empty();
        return Optional.ofNullable((UsuarioAutenticado) attrs.getAttribute(ATRIBUTO, RequestAttributes.SCOPE_REQUEST));
    }

    /** 401 si no hay sesión. */
    public static UsuarioAutenticado actual() {
        return actualOpcional().orElseThrow(() -> ApiException.noAutenticado("Debes iniciar sesión"));
    }

    /** 401 si no hay sesión, 403 si el rol no está permitido. */
    public static UsuarioAutenticado requerirRol(Rol... roles) {
        UsuarioAutenticado u = actual();
        if (Arrays.stream(roles).noneMatch(r -> r == u.rol())) {
            throw ApiException.prohibido("No tienes permisos para realizar esta acción");
        }
        return u;
    }

    /** Permite al propio usuario o a un administrador. */
    public static UsuarioAutenticado requerirMismoUsuarioOAdmin(Long usuarioId) {
        UsuarioAutenticado u = actual();
        if (u.rol() != Rol.ADMINISTRADOR && !u.id().equals(usuarioId)) {
            throw ApiException.prohibido("No tienes permisos para realizar esta acción");
        }
        return u;
    }
}
