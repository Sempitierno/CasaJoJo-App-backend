package cl.lacasajojo.usuarios.dto;

import jakarta.validation.constraints.Size;

/** Nombre del archivo de la foto; null la quita. */
public record FotoRequest(@Size(max = 255, message = "Nombre de archivo demasiado largo") String foto) {}
