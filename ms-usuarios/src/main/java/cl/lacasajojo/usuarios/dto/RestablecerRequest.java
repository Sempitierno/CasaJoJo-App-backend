package cl.lacasajojo.usuarios.dto;

import jakarta.validation.constraints.*;

public record RestablecerRequest(
        @NotBlank(message = "Ingresa tu correo electrónico")
        @Email(message = "Ingresa un correo válido, por ejemplo nombre@correo.cl")
        String correo,

        @NotBlank(message = "Ingresa el código de recuperación")
        @Pattern(regexp = "\\d{6}", message = "El código tiene 6 dígitos")
        String codigo,

        @NotBlank(message = "Ingresa una contraseña")
        @Size(min = 6, max = 72, message = "La contraseña debe tener entre 6 y 72 caracteres")
        String nuevaPassword
) {
    public RestablecerRequest {
        correo = correo == null ? null : correo.trim();
    }
}
