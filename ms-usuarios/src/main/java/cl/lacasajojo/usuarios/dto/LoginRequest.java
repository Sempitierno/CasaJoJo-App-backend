package cl.lacasajojo.usuarios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Ingresa tu correo electrónico")
        @Email(message = "Ingresa un correo válido, por ejemplo nombre@correo.cl")
        String correo,

        @NotBlank(message = "Ingresa tu contraseña")
        String password
) {
    public LoginRequest {
        correo = correo == null ? null : correo.trim();
    }
}
