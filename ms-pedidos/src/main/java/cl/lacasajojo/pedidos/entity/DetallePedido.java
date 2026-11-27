package cl.lacasajojo.pedidos.entity;

import cl.lacasajojo.common.modelo.CategoriaProducto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Línea del pedido con copia del nombre y precio (cambiar el menú no altera pedidos antiguos). */
@Entity @Table(name = "detalles_pedido")
@Getter @Setter @NoArgsConstructor
public class DetallePedido {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private CategoriaProducto categoria;

    private Long productoId;

    @Column(nullable = false, length = 60)
    private String nombreProducto;

    @Column(nullable = false)
    private Integer precioUnitario;
}
