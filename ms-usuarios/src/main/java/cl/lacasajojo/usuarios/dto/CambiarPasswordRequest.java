package cl.lacasajojo.usuarios.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CambiarPasswordRequest(
        @NotBlank(message = "Ingresa tu contraseña actual")
        String passwordActual,

        @NotBlank(message = "Ingresa una contraseña")
        @Size(min = 6, max = 72, message = "La contraseña debe tener entre 6 y 72 caracteres")
        String nuevaPassword
) {}
