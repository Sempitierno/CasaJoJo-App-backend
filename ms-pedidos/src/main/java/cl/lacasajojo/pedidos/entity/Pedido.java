package cl.lacasajojo.pedidos.entity;

import cl.lacasajojo.common.modelo.EstadoPedido;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Pedido (comanda). El id es un UUID generado por la app, así un doble toque en "Confirmar"
 * no duplica el pedido. clienteId apunta a un usuario de ms-usuarios (otra base de datos, por eso
 * no es clave foránea); nombre y correo se copian al crear el pedido.
 */
@Entity
@Table(name = "pedidos", indexes = {
        @Index(name = "idx_pedidos_cliente", columnList = "cliente_id"),
        @Index(name = "idx_pedidos_estado", columnList = "estado")
})
@Getter @Setter @NoArgsConstructor
public class Pedido {
    @Id @Column(length = 36)
    private String id;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(nullable = false, length = 80)
    private String clienteNombre;

    @Column(nullable = false, length = 254)
    private String clienteCorreo;

    @Column(nullable = false)
    private Long creadoEn;

    @Column(nullable = false)
    private Long horaRetiro;

    @Column(nullable = false, length = 300)
    private String especificaciones = "";

    @Column(nullable = false)
    private Integer total;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private EstadoPedido estado;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private EstadoPedido estadoNotificado;

    @Column(nullable = false)
    private Long actualizadoEn;

    @Column(length = 255)
    private String fotoPedido;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<DetallePedido> detalles = new ArrayList<>();

    public void agregarDetalle(DetallePedido d) {
        d.setPedido(this);
        detalles.add(d);
    }
}
