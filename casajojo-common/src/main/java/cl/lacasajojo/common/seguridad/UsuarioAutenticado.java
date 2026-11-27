package cl.lacasajojo.common.seguridad;

import cl.lacasajojo.common.modelo.Rol;

/** Datos del usuario que vienen dentro del token JWT. */
public record UsuarioAutenticado(Long id, String correo, Rol rol) {}
