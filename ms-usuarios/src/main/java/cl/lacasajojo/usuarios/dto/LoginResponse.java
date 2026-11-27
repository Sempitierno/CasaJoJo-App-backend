package cl.lacasajojo.usuarios.dto;

/** token: se envía en cada petición como "Authorization: Bearer <token>". */
public record LoginResponse(String token, Long expiraEn, UsuarioDto usuario) {}
