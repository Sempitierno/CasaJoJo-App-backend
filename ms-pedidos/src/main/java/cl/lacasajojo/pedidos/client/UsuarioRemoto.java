package cl.lacasajojo.pedidos.client;

import cl.lacasajojo.common.modelo.Rol;

/** Lo que ms-pedidos necesita de un usuario de ms-usuarios. */
public record UsuarioRemoto(Long id, String nombreCompleto, String correo, Rol rol) {}
