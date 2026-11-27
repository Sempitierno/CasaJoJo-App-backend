package cl.lacasajojo.usuarios.controller;

import cl.lacasajojo.common.error.ApiException;
import cl.lacasajojo.common.modelo.Rol;
import cl.lacasajojo.common.seguridad.Seguridad;
import cl.lacasajojo.usuarios.dto.*;
import cl.lacasajojo.usuarios.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    /** Solo administrador. ?rol=COCINERO para filtrar. */
    @GetMapping
    public List<UsuarioDto> listar(@RequestParam(required = false) Rol rol) {
        Seguridad.requerirRol(Rol.ADMINISTRADOR);
        return usuarioService.listar(rol);
    }

    /** Solo administrador: crea usuarios con cualquier rol. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioDto crear(@Valid @RequestBody CrearUsuarioRequest req) {
        Seguridad.requerirRol(Rol.ADMINISTRADOR);
        return usuarioService.crear(req.nombreCompleto(), req.correo(), req.password(), req.rol());
    }

    @GetMapping("/{id}")
    public UsuarioDto obtener(@PathVariable Long id) {
        Seguridad.requerirMismoUsuarioOAdmin(id);
        return usuarioService.obtener(id);
    }

    /** Modificar perfil: el propio usuario o el administrador. */
    @PutMapping("/{id}")
    public UsuarioDto actualizarPerfil(@PathVariable Long id, @Valid @RequestBody ActualizarPerfilRequest req) {
        Seguridad.requerirMismoUsuarioOAdmin(id);
        return usuarioService.actualizarPerfil(id, req);
    }

    /** Solo el propio usuario, y debe conocer su contraseña actual. */
    @PutMapping("/{id}/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cambiarPassword(@PathVariable Long id, @Valid @RequestBody CambiarPasswordRequest req) {
        if (!Seguridad.actual().id().equals(id)) {
            throw ApiException.prohibido("Solo puedes cambiar tu propia contraseña");
        }
        usuarioService.cambiarPassword(id, req);
    }

    @PatchMapping("/{id}/foto")
    public UsuarioDto cambiarFoto(@PathVariable Long id, @Valid @RequestBody FotoRequest req) {
        Seguridad.requerirMismoUsuarioOAdmin(id);
        return usuarioService.cambiarFoto(id, req.foto());
    }

    @PatchMapping("/{id}/rol")
    public UsuarioDto cambiarRol(@PathVariable Long id, @Valid @RequestBody CambiarRolRequest req) {
        Seguridad.requerirRol(Rol.ADMINISTRADOR);
        return usuarioService.cambiarRol(id, req.rol());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        Seguridad.requerirRol(Rol.ADMINISTRADOR);
        usuarioService.eliminar(id);
    }
}
