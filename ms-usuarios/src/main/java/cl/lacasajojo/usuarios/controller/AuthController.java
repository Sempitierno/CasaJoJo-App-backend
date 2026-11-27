package cl.lacasajojo.usuarios.controller;

import cl.lacasajojo.common.modelo.Rol;
import cl.lacasajojo.common.seguridad.Seguridad;
import cl.lacasajojo.usuarios.dto.*;
import cl.lacasajojo.usuarios.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** Endpoints públicos de autenticación (no requieren token, salvo /yo). */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService usuarioService;

    /** El registro público crea CLIENTES. Para crear personal (cocinero, cajero, admin) se necesita token de administrador. */
    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioDto registrar(@Valid @RequestBody RegistroRequest req) {
        Rol rol = req.rol() == null ? Rol.CLIENTE : req.rol();
        if (rol != Rol.CLIENTE) Seguridad.requerirRol(Rol.ADMINISTRADOR);
        return usuarioService.crear(req.nombreCompleto(), req.correo(), req.password(), rol);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        return usuarioService.login(req);
    }

    /** Perfil del usuario dueño del token. */
    @GetMapping("/yo")
    public UsuarioDto yo() {
        return usuarioService.obtener(Seguridad.actual().id());
    }

    @PostMapping("/recuperar")
    public RecuperarResponse recuperar(@Valid @RequestBody RecuperarRequest req) {
        return usuarioService.solicitarRecuperacion(req);
    }

    @PostMapping("/restablecer")
    public Map<String, String> restablecer(@Valid @RequestBody RestablecerRequest req) {
        usuarioService.restablecerPassword(req);
        return Map.of("mensaje", "Contraseña actualizada. Ya puedes iniciar sesión.");
    }
}
