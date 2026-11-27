package cl.lacasajojo.pedidos.controller;

import cl.lacasajojo.pedidos.service.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** Solo para ms-usuarios (X-Internal-Key): saber si un usuario tiene pedidos antes de borrarlo. */
@RestController
@RequestMapping("/internal/pedidos")
@RequiredArgsConstructor
public class InternoPedidoController {

    private final PedidoService pedidoService;

    @GetMapping("/existe")
    public Map<String, Boolean> existe(@RequestParam Long clienteId) {
        return Map.of("existe", pedidoService.clienteTienePedidos(clienteId));
    }
}
