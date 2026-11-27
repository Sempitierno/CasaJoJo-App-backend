package cl.lacasajojo.productos.entity;

import cl.lacasajojo.common.modelo.CategoriaProducto;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "productos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Producto {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String nombre;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private CategoriaProducto categoria;

    @Builder.Default
    @Column(nullable = false, length = 160)
    private String descripcion = "";

    /** Pesos chilenos enteros. */
    @Column(nullable = false)
    private Integer precio;

    @Builder.Default
    @Column(nullable = false)
    private boolean disponible = true;

    @Builder.Default
    @Column(nullable = false)
    private boolean publicado = true;

    @Column(length = 255)
    private String fotoProducto;
}
