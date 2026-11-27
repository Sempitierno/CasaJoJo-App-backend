package cl.lacasajojo.productos.dto;

import jakarta.validation.constraints.NotNull;

/** {"valor": true} */
public record CambioBooleanoRequest(@NotNull(message = "Falta el valor") Boolean valor) {}
