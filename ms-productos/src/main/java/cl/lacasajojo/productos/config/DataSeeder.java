package cl.lacasajojo.productos.config;

import cl.lacasajojo.common.modelo.CategoriaProducto;
import cl.lacasajojo.productos.entity.Producto;
import cl.lacasajojo.productos.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/** Mismo menú de ejemplo que DatosDemostracion de la app; solo si no hay productos. */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private static final String NOTA = "Producto de demostración (datos ficticios).";

    private final ProductoRepository productoRepository;

    @Value("${app.seed.enabled:true}")
    private boolean habilitado;

    @Override
    public void run(String... args) {
        if (!habilitado || productoRepository.count() > 0) return;
        productoRepository.saveAll(List.of(
                p("Pollo asado (demo)", CategoriaProducto.PLATO_FONDO, 4500, true),
                p("Pastel de choclo (demo)", CategoriaProducto.PLATO_FONDO, 4200, true),
                p("Lasaña de verduras (demo)", CategoriaProducto.PLATO_FONDO, 4300, true),
                p("Arroz graneado (demo)", CategoriaProducto.ACOMPANAMIENTO, 1000, true),
                p("Puré de papas (demo)", CategoriaProducto.ACOMPANAMIENTO, 1200, true),
                p("Papas fritas (demo)", CategoriaProducto.ACOMPANAMIENTO, 1500, false),
                p("Ensalada chilena (demo)", CategoriaProducto.ENSALADA, 1200, true),
                p("Ensalada de lechuga y palta (demo)", CategoriaProducto.ENSALADA, 1800, true),
                p("Leche asada (demo)", CategoriaProducto.POSTRE, 1300, true),
                p("Fruta de la estación (demo)", CategoriaProducto.POSTRE, 900, true),
                p("Jugo natural de frutilla (demo)", CategoriaProducto.BEBIDA, 1500, true),
                p("Agua mineral (demo)", CategoriaProducto.BEBIDA, 1000, true)
        ));
        log.info("Menú de demostración cargado");
    }

    private static Producto p(String nombre, CategoriaProducto c, int precio, boolean disponible) {
        return Producto.builder().nombre(nombre).categoria(c).descripcion(NOTA)
                .precio(precio).disponible(disponible).publicado(true).build();
    }
}
