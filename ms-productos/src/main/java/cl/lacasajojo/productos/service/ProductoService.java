package cl.lacasajojo.productos.service;

import cl.lacasajojo.common.error.ApiException;
import cl.lacasajojo.productos.dto.ProductoDto;
import cl.lacasajojo.productos.dto.ProductoRequest;
import cl.lacasajojo.productos.entity.Producto;
import cl.lacasajojo.productos.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ProductoService {

    /** Mismo orden que la app: por categoría y luego por nombre. */
    static final Comparator<Producto> ORDEN_MINUTA = Comparator
            .comparing((Producto p) -> p.getCategoria().ordinal())
            .thenComparing(p -> p.getNombre().toLowerCase(Locale.ROOT));

    private final ProductoRepository productoRepository;

    @Transactional(readOnly = true)
    public List<ProductoDto> listar(boolean soloPublicados) {
        List<Producto> productos = soloPublicados ? productoRepository.findByPublicadoTrue() : productoRepository.findAll();
        return productos.stream().sorted(ORDEN_MINUTA).map(ProductoDto::de).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductoDto> porIds(Collection<Long> ids) {
        return productoRepository.findAllById(ids).stream().map(ProductoDto::de).toList();
    }

    @Transactional(readOnly = true)
    public ProductoDto obtener(Long id) {
        return ProductoDto.de(buscar(id));
    }

    @Transactional
    public ProductoDto crear(ProductoRequest req) {
        Producto p = new Producto();
        aplicar(p, req);
        return ProductoDto.de(productoRepository.save(p));
    }

    @Transactional
    public ProductoDto actualizar(Long id, ProductoRequest req) {
        Producto p = buscar(id);
        aplicar(p, req);
        return ProductoDto.de(p);
    }

    /** Los pedidos guardan su propia copia de nombre y precio, así que no se alteran. */
    @Transactional
    public void eliminar(Long id) {
        productoRepository.delete(buscar(id));
    }

    @Transactional
    public ProductoDto cambiarPublicado(Long id, boolean publicado) {
        Producto p = buscar(id);
        p.setPublicado(publicado);
        return ProductoDto.de(p);
    }

    @Transactional
    public ProductoDto cambiarDisponible(Long id, boolean disponible) {
        Producto p = buscar(id);
        p.setDisponible(disponible);
        return ProductoDto.de(p);
    }

    private void aplicar(Producto p, ProductoRequest req) {
        String nombre = req.nombre().trim();
        if (nombre.isEmpty()) throw ApiException.campo("nombre", "Ingresa el nombre del producto");
        p.setNombre(nombre);
        p.setCategoria(req.categoria());
        p.setDescripcion(req.descripcion() == null ? "" : req.descripcion().trim());
        p.setPrecio(req.precio());
        p.setDisponible(req.disponible() == null || req.disponible());
        p.setPublicado(req.publicado() == null || req.publicado());
        p.setFotoProducto(req.fotoProducto());
    }

    private Producto buscar(Long id) {
        return productoRepository.findById(id).orElseThrow(() -> ApiException.noEncontrado("Producto no encontrado"));
    }
}
