package cl.lacasajojo.pedidos.dto;

import jakarta.validation.constraints.Size;

public record FotoRequest(@Size(max = 255, message = "Nombre de archivo demasiado largo") String foto) {}
