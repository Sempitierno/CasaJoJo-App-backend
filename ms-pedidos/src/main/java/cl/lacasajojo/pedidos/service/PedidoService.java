package cl.lacasajojo.pedidos.service;

import cl.lacasajojo.common.error.ApiException;
import cl.lacasajojo.common.modelo.CategoriaProducto;
import cl.lacasajojo.common.modelo.EstadoPedido;
import cl.lacasajojo.common.modelo.Rol;
import cl.lacasajojo.common.seguridad.UsuarioAutenticado;
import cl.lacasajojo.pedidos.client.ProductoRemoto;
import cl.lacasajojo.pedidos.client.ProductosClient;
import cl.lacasajojo.pedidos.client.UsuarioRemoto;
import cl.lacasajojo.pedidos.client.UsuariosClient;
import cl.lacasajojo.pedidos.dto.CrearPedidoRequest;
import cl.lacasajojo.pedidos.dto.PedidoDto;
import cl.lacasajojo.pedidos.entity.DetallePedido;
import cl.lacasajojo.pedidos.entity.Pedido;
import cl.lacasajojo.pedidos.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Reglas de negocio de pedidos (las mismas del PedidoRepository de la app) y permisos por rol. */
@Service
@RequiredArgsConstructor
public class PedidoService {

    private static final Set<EstadoPedido> ESTADOS_COCINA =
            EnumSet.of(EstadoPedido.RECIBIDO, EstadoPedido.EN_PREPARACION, EstadoPedido.LISTO_RETIRO);

    private final PedidoRepository pedidoRepository;
    private final UsuariosClient usuariosClient;
    private final ProductosClient productosClient;
    private final Clock clock;

    @Transactional(readOnly = true)
    public List<PedidoDto> listar(Long clienteId, EstadoPedido estado) {
        List<Pedido> pedidos;
        if (clienteId != null) {
            pedidos = pedidoRepository.findByClienteIdOrderByCreadoEnDesc(clienteId);
            if (estado != null) pedidos = pedidos.stream().filter(p -> p.getEstado() == estado).toList();
        } else if (estado != null) {
            pedidos = pedidoRepository.findByEstadoOrderByCreadoEnDesc(estado);
        } else {
            pedidos = pedidoRepository.findAllByOrderByCreadoEnDesc();
        }
        return pedidos.stream().map(PedidoDto::de).toList();
    }

    /** El cliente solo ve sus pedidos (para él, un pedido ajeno "no existe"). El personal ve todos. */
    @Transactional(readOnly = true)
    public PedidoDto obtener(String id, UsuarioAutenticado quien) {
        Pedido p = buscar(id);
        if (quien.rol() == Rol.CLIENTE && !quien.id().equals(p.getClienteId())) {
            throw ApiException.noEncontrado("Pedido no encontrado");
        }
        return PedidoDto.de(p);
    }

    @Transactional(readOnly = true)
    public List<PedidoDto> pendientesDeNotificar(Long clienteId) {
        return pedidoRepository.findByClienteIdOrderByCreadoEnDesc(clienteId).stream()
                .filter(p -> p.getEstado() != p.getEstadoNotificado())
                .map(PedidoDto::de).toList();
    }

    @Transactional(readOnly = true)
    public boolean clienteTienePedidos(Long clienteId) {
        return pedidoRepository.existsByClienteId(clienteId);
    }

    /**
     * Crea un pedido en estado RECIBIDO para el cliente del token. Valida reglas, consulta a ms-usuarios
     * (datos del cliente) y a ms-productos (precios y disponibilidad vigentes) y calcula el total.
     * Si el id ya existe (doble toque), devuelve el mismo pedido sin duplicarlo.
     */
    @Transactional
    public Resultado crear(CrearPedidoRequest req, Long clienteId) {
        Optional<Pedido> existente = pedidoRepository.findWithDetallesById(req.id());
        if (existente.isPresent()) {
            if (!existente.get().getClienteId().equals(clienteId)) {
                throw ApiException.conflicto("Ya existe un pedido con ese id");
            }
            return new Resultado(PedidoDto.de(existente.get()), false);
        }

        long ahora = clock.millis();
        Map<String, String> errores = new LinkedHashMap<>();
        if (!req.seleccion().containsKey(CategoriaProducto.PLATO_FONDO)) {
            errores.put("platoDeFondo", "Elige un plato de fondo para continuar");
        }
        if (req.horaRetiro() < ahora / 60_000L * 60_000L) {
            errores.put("horaRetiro", "La hora de retiro no puede ser anterior a la hora actual");
        }
        String especificaciones = req.especificaciones() == null ? "" : req.especificaciones().trim();
        if (!errores.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, errores.values().iterator().next(), errores);
        }

        UsuarioRemoto cliente = usuariosClient.obtener(clienteId)
                .orElseThrow(() -> ApiException.noEncontrado("Cliente no encontrado"));
        Map<Long, ProductoRemoto> productos = productosClient.porIds(new HashSet<>(req.seleccion().values())).stream()
                .collect(Collectors.toMap(ProductoRemoto::id, Function.identity()));

        Pedido pedido = new Pedido();
        pedido.setId(req.id());
        pedido.setClienteId(cliente.id());
        pedido.setClienteNombre(cliente.nombreCompleto());
        pedido.setClienteCorreo(cliente.correo());
        pedido.setCreadoEn(ahora);
        pedido.setHoraRetiro(req.horaRetiro());
        pedido.setEspecificaciones(especificaciones);
        pedido.setEstado(EstadoPedido.RECIBIDO);
        pedido.setEstadoNotificado(EstadoPedido.RECIBIDO);
        pedido.setActualizadoEn(ahora);

        int total = 0;
        for (CategoriaProducto categoria : new TreeSet<>(req.seleccion().keySet())) {
            Long productoId = req.seleccion().get(categoria);
            ProductoRemoto producto = productoId == null ? null : productos.get(productoId);
            if (producto == null || producto.categoria() != categoria || !producto.publicado() || !producto.disponible()) {
                String nombre = producto == null ? "seleccionado" : producto.nombre();
                throw ApiException.conflicto("El producto " + nombre
                        + " ya no está disponible. Revisa tu selección e inténtalo de nuevo.");
            }
            DetallePedido d = new DetallePedido();
            d.setCategoria(categoria);
            d.setProductoId(producto.id());
            d.setNombreProducto(producto.nombre());
            d.setPrecioUnitario(producto.precio());
            pedido.agregarDetalle(d);
            total += producto.precio();
        }
        pedido.setTotal(total);
        return new Resultado(PedidoDto.de(pedidoRepository.save(pedido)), true);
    }

    /**
     * Permisos por rol para cambiar el estado:
     * ADMINISTRADOR cualquiera; COCINERO entre RECIBIDO, EN_PREPARACION y LISTO_RETIRO (no toca entregados);
     * CAJERO solo LISTO_RETIRO → ENTREGADO (o deshacer ENTREGADO → LISTO_RETIRO).
     */
    @Transactional
    public PedidoDto cambiarEstado(String id, EstadoPedido nuevo, UsuarioAutenticado quien) {
        Pedido p = buscar(id);
        EstadoPedido actual = p.getEstado();
        boolean permitido = switch (quien.rol()) {
            case ADMINISTRADOR -> true;
            case COCINERO -> ESTADOS_COCINA.contains(actual) && ESTADOS_COCINA.contains(nuevo);
            case CAJERO -> (actual == EstadoPedido.LISTO_RETIRO && nuevo == EstadoPedido.ENTREGADO)
                    || (actual == EstadoPedido.ENTREGADO && nuevo == EstadoPedido.LISTO_RETIRO);
            case CLIENTE -> false;
        };
        if (!permitido) {
            throw ApiException.prohibido("Tu rol no puede cambiar el pedido de " + actual + " a " + nuevo);
        }
        p.setEstado(nuevo);
        p.setActualizadoEn(clock.millis());
        return PedidoDto.de(p);
    }

    @Transactional
    public PedidoDto marcarNotificado(String id, EstadoPedido estado, Long clienteId) {
        Pedido p = buscar(id);
        if (!p.getClienteId().equals(clienteId)) throw ApiException.noEncontrado("Pedido no encontrado");
        p.setEstadoNotificado(estado);
        return PedidoDto.de(p);
    }

    @Transactional
    public PedidoDto asignarFoto(String id, String foto) {
        Pedido p = buscar(id);
        p.setFotoPedido(foto);
        p.setActualizadoEn(clock.millis());
        return PedidoDto.de(p);
    }

    @Transactional
    public void eliminar(String id) {
        pedidoRepository.delete(buscar(id));
    }

    private Pedido buscar(String id) {
        return pedidoRepository.findWithDetallesById(id).orElseThrow(() -> ApiException.noEncontrado("Pedido no encontrado"));
    }

    /** creado = false cuando el id ya existía (reintento). */
    public record Resultado(PedidoDto pedido, boolean creado) {}
}
