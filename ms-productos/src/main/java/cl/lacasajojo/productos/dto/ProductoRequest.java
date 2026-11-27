package cl.lacasajojo.productos.dto;

import cl.lacasajojo.common.modelo.CategoriaProducto;
import jakarta.validation.constraints.*;

public record ProductoRequest(
        @NotBlank(message = "Ingresa el nombre del producto")
        @Size(max = 60, message = "Máximo 60 caracteres")
        String nombre,

        @NotNull(message = "Selecciona una categoría")
        CategoriaProducto categoria,

        @Size(max = 160, message = "Máximo 160 caracteres")
        String descripcion,

        @NotNull(message = "Ingresa el precio")
        @Positive(message = "El precio debe ser mayor que cero")
        @Max(value = 999_999, message = "El precio máximo es $999.999")
        Integer precio,

        Boolean disponible,
        Boolean publicado,

        @Size(max = 255, message = "Nombre de archivo demasiado largo")
        String fotoProducto
) {}
