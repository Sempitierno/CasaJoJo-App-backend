package cl.lacasajojo.usuarios.entity;

import cl.lacasajojo.common.modelo.Rol;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "usuarios", uniqueConstraints = @UniqueConstraint(name = "uk_usuarios_correo", columnNames = "correo"))
@Getter @Setter @NoArgsConstructor
public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String nombreCompleto;

    /** Siempre en minúsculas (se normaliza en el servicio). */
    @Column(nullable = false, length = 254)
    private String correo;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private Rol rol;

    /** BCrypt, nunca texto plano. Nunca sale en los DTO. */
    @Column(nullable = false, length = 100)
    private String claveHash;

    /** Milisegundos desde epoch, igual que en Room. */
    @Column(nullable = false)
    private Long creadoEn;

    @Column(length = 255)
    private String fotoPerfil;

    /** Código de recuperación (hash BCrypt) y su vencimiento. */
    @Column(length = 100)
    private String codigoRecuperacionHash;

    private Long codigoRecuperacionExpiraEn;
}
