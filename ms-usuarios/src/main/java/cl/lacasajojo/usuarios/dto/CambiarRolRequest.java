package cl.lacasajojo.usuarios.dto;

import cl.lacasajojo.common.modelo.Rol;
import jakarta.validation.constraints.NotNull;

public record CambiarRolRequest(@NotNull(message = "Selecciona el rol") Rol rol) {}
