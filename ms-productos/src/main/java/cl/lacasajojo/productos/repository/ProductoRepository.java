package cl.lacasajojo.productos.repository;

import cl.lacasajojo.productos.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findByPublicadoTrue();
}
