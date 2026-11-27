package cl.lacasajojo.usuarios.dto;

import cl.lacasajojo.common.modelo.Rol;
import cl.lacasajojo.usuarios.entity.Usuario;

/** Datos públicos del usuario (PerfilUsuario en la app). Nunca incluye la clave. */
public record UsuarioDto(Long id, String nombreCompleto, String correo, Rol rol, String fotoPerfil, Long creadoEn) {
    public static UsuarioDto de(Usuario u) {
        return new UsuarioDto(u.getId(), u.getNombreCompleto(), u.getCorreo(), u.getRol(), u.getFotoPerfil(), u.getCreadoEn());
    }
}
