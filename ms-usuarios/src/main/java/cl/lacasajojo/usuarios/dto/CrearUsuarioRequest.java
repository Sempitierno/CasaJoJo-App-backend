package cl.lacasajojo.usuarios.dto;

import cl.lacasajojo.common.modelo.Rol;
import jakarta.validation.constraints.*;

/** Alta de usuarios por el administrador (por ejemplo cocineros y cajeros). */
public record CrearUsuarioRequest(
        @NotBlank(message = "Ingresa el nombre completo")
        @Size(min = 3, max = 80, message = "El nombre debe tener entre 3 y 80 caracteres")
        String nombreCompleto,

        @NotBlank(message = "Ingresa el correo electrónico")
        @Email(message = "Ingresa un correo válido, por ejemplo nombre@correo.cl")
        @Size(max = 254, message = "El correo es demasiado largo")
        String correo,

        @NotBlank(message = "Ingresa una contraseña")
        @Size(min = 6, max = 72, message = "La contraseña debe tener entre 6 y 72 caracteres")
        String password,

        @NotNull(message = "Selecciona el rol")
        Rol rol
) {
    public CrearUsuarioRequest {
        correo = correo == null ? null : correo.trim();
    }
}
