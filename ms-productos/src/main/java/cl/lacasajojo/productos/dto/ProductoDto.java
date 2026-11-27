package cl.lacasajojo.productos.dto;

import cl.lacasajojo.common.modelo.CategoriaProducto;
import cl.lacasajojo.productos.entity.Producto;

public record ProductoDto(Long id, String nombre, CategoriaProducto categoria, String descripcion,
                          Integer precio, boolean disponible, boolean publicado, String fotoProducto) {
    public static ProductoDto de(Producto p) {
        return new ProductoDto(p.getId(), p.getNombre(), p.getCategoria(), p.getDescripcion(),
                p.getPrecio(), p.isDisponible(), p.isPublicado(), p.getFotoProducto());
    }
}
