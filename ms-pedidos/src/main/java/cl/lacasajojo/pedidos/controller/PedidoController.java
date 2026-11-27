package cl.lacasajojo.pedidos.controller;

import cl.lacasajojo.common.modelo.EstadoPedido;
import cl.lacasajojo.common.modelo.Rol;
import cl.lacasajojo.common.seguridad.Seguridad;
import cl.lacasajojo.common.seguridad.UsuarioAutenticado;
import cl.lacasajojo.pedidos.dto.CambiarEstadoRequest;
import cl.lacasajojo.pedidos.dto.CrearPedidoRequest;
import cl.lacasajojo.pedidos.dto.FotoRequest;
import cl.lacasajojo.pedidos.dto.PedidoDto;
import cl.lacasajojo.pedidos.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Todos los endpoints exigen token. */
@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    /** Personal: todos (filtros ?clienteId= y ?estado=). Cliente: siempre solo los suyos. */
    @GetMapping
    public List<PedidoDto> listar(@RequestParam(required = false) Long clienteId,
                                  @RequestParam(required = false) EstadoPedido estado) {
        UsuarioAutenticado u = Seguridad.actual();
        Long filtro = u.rol() == Rol.CLIENTE ? u.id() : clienteId;
        return pedidoService.listar(filtro, estado);
    }

    @GetMapping("/{id}")
    public PedidoDto obtener(@PathVariable String id) {
        return pedidoService.obtener(id, Seguridad.actual());
    }

    /** Pedidos del usuario del token con cambios de estado sin notificar. */
    @GetMapping("/pendientes-notificar")
    public List<PedidoDto> pendientesDeNotificar() {
        return pedidoService.pendientesDeNotificar(Seguridad.actual().id());
    }

    /** Solo clientes. 201 si se creó; 200 con el mismo pedido si el id ya existía. */
    @PostMapping
    public ResponseEntity<PedidoDto> crear(@Valid @RequestBody CrearPedidoRequest req) {
        UsuarioAutenticado u = Seguridad.requerirRol(Rol.CLIENTE);
        PedidoService.Resultado r = pedidoService.crear(req, u.id());
        return ResponseEntity.status(r.creado() ? HttpStatus.CREATED : HttpStatus.OK).body(r.pedido());
    }

    /** Administrador, cocinero o cajero (cada uno con sus transiciones permitidas). */
    @PatchMapping("/{id}/estado")
    public PedidoDto cambiarEstado(@PathVariable String id, @Valid @RequestBody CambiarEstadoRequest req) {
        return pedidoService.cambiarEstado(id, req.estado(), Seguridad.actual());
    }

    /** El cliente marca que ya vio la notificación del estado. */
    @PatchMapping("/{id}/notificado")
    public PedidoDto marcarNotificado(@PathVariable String id, @Valid @RequestBody CambiarEstadoRequest req) {
        return pedidoService.marcarNotificado(id, req.estado(), Seguridad.actual().id());
    }

    /** Foto del pedido terminado: administrador o cocinero. */
    @PatchMapping("/{id}/foto")
    public PedidoDto asignarFoto(@PathVariable String id, @Valid @RequestBody FotoRequest req) {
        Seguridad.requerirRol(Rol.ADMINISTRADOR, Rol.COCINERO);
        return pedidoService.asignarFoto(id, req.foto());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable String id) {
        Seguridad.requerirRol(Rol.ADMINISTRADOR);
        pedidoService.eliminar(id);
    }
}
