package cl.lacasajojo.usuarios.dto;

import jakarta.validation.constraints.*;

public record ActualizarPerfilRequest(
        @NotBlank(message = "Ingresa tu nombre completo")
        @Size(min = 3, max = 80, message = "El nombre debe tener entre 3 y 80 caracteres")
        String nombreCompleto,

        @NotBlank(message = "Ingresa tu correo electrónico")
        @Email(message = "Ingresa un correo válido, por ejemplo nombre@correo.cl")
        @Size(max = 254, message = "El correo es demasiado largo")
        String correo
) {
    public ActualizarPerfilRequest {
        correo = correo == null ? null : correo.trim();
    }
}
