package cl.lacasajojo.pedidos.dto;

import cl.lacasajojo.common.modelo.EstadoPedido;
import jakarta.validation.constraints.NotNull;

public record CambiarEstadoRequest(@NotNull(message = "Falta el estado") EstadoPedido estado) {}
