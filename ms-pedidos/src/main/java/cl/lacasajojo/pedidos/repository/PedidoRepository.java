package cl.lacasajojo.pedidos.repository;

import cl.lacasajojo.common.modelo.EstadoPedido;
import cl.lacasajojo.pedidos.entity.Pedido;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, String> {

    @EntityGraph(attributePaths = "detalles")
    List<Pedido> findAllByOrderByCreadoEnDesc();

    @EntityGraph(attributePaths = "detalles")
    List<Pedido> findByClienteIdOrderByCreadoEnDesc(Long clienteId);

    @EntityGraph(attributePaths = "detalles")
    List<Pedido> findByEstadoOrderByCreadoEnDesc(EstadoPedido estado);

    @EntityGraph(attributePaths = "detalles")
    Optional<Pedido> findWithDetallesById(String id);

    boolean existsByClienteId(Long clienteId);
}
