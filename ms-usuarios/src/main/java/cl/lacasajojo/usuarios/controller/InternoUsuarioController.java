package cl.lacasajojo.usuarios.controller;

import cl.lacasajojo.usuarios.dto.UsuarioDto;
import cl.lacasajojo.usuarios.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/** Solo para otros microservicios (protegido por X-Internal-Key; el gateway no lo publica). */
@RestController
@RequestMapping("/internal/usuarios")
@RequiredArgsConstructor
public class InternoUsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping("/{id}")
    public UsuarioDto obtener(@PathVariable Long id) {
        return usuarioService.obtener(id);
    }
}
