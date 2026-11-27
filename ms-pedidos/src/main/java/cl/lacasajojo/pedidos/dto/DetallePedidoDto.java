package cl.lacasajojo.pedidos.dto;

import cl.lacasajojo.common.modelo.CategoriaProducto;
import cl.lacasajojo.pedidos.entity.DetallePedido;

public record DetallePedidoDto(Long id, String pedidoId, CategoriaProducto categoria, Long productoId,
                               String nombreProducto, Integer precioUnitario) {
    public static DetallePedidoDto de(DetallePedido d) {
        return new DetallePedidoDto(d.getId(), d.getPedido().getId(), d.getCategoria(), d.getProductoId(),
                d.getNombreProducto(), d.getPrecioUnitario());
    }
}
