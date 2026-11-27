package cl.lacasajojo.usuarios.dto;

import cl.lacasajojo.common.modelo.Rol;
import jakarta.validation.constraints.*;

/** Registro público. rol es opcional: si falta es CLIENTE; otros roles solo los puede asignar un administrador. */
public record RegistroRequest(
        @NotBlank(message = "Ingresa tu nombre completo")
        @Size(min = 3, max = 80, message = "El nombre debe tener entre 3 y 80 caracteres")
        String nombreCompleto,

        @NotBlank(message = "Ingresa tu correo electrónico")
        @Email(message = "Ingresa un correo válido, por ejemplo nombre@correo.cl")
        @Size(max = 254, message = "El correo es demasiado largo")
        String correo,

        @NotBlank(message = "Ingresa una contraseña")
        @Size(min = 6, max = 72, message = "La contraseña debe tener entre 6 y 72 caracteres")
        String password,

        Rol rol
) {
    public RegistroRequest {
        correo = correo == null ? null : correo.trim();
    }
}
