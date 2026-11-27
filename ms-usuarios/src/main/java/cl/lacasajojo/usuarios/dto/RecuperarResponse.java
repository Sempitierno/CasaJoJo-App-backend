package cl.lacasajojo.usuarios.dto;

/**
 * Como el proyecto no tiene servidor de correo, el código se devuelve en "codigoDemo" para probar
 * el flujo en la defensa (app.recuperacion.mostrar-codigo=true). En producción se enviaría por correo.
 */
public record RecuperarResponse(String mensaje, String codigoDemo, Long expiraEn) {}
