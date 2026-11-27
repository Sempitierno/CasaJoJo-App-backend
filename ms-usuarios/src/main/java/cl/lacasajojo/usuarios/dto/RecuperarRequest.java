package cl.lacasajojo.usuarios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RecuperarRequest(
        @NotBlank(message = "Ingresa tu correo electrónico")
        @Email(message = "Ingresa un correo válido, por ejemplo nombre@correo.cl")
        String correo
) {
    public RecuperarRequest {
        correo = correo == null ? null : correo.trim();
    }
}
