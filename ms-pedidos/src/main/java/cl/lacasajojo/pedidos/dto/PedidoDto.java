package cl.lacasajojo.pedidos.dto;

import cl.lacasajojo.common.modelo.EstadoPedido;
import cl.lacasajojo.pedidos.entity.Pedido;

import java.util.List;

/** Pedido con sus líneas (PedidoConDetalle en Room). */
public record PedidoDto(String id, Long clienteId, String clienteNombre, String clienteCorreo,
                        Long creadoEn, Long horaRetiro, String especificaciones, Integer total,
                        EstadoPedido estado, EstadoPedido estadoNotificado, Long actualizadoEn,
                        String fotoPedido, List<DetallePedidoDto> detalles) {
    public static PedidoDto de(Pedido p) {
        return new PedidoDto(p.getId(), p.getClienteId(), p.getClienteNombre(), p.getClienteCorreo(),
                p.getCreadoEn(), p.getHoraRetiro(), p.getEspecificaciones(), p.getTotal(),
                p.getEstado(), p.getEstadoNotificado(), p.getActualizadoEn(), p.getFotoPedido(),
                p.getDetalles().stream().map(DetallePedidoDto::de).toList());
    }
}
