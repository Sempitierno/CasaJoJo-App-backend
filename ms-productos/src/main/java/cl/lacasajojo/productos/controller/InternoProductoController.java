package cl.lacasajojo.productos.controller;

import cl.lacasajojo.productos.dto.ProductoDto;
import cl.lacasajojo.productos.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Solo para ms-pedidos (X-Internal-Key). */
@RestController
@RequestMapping("/internal/productos")
@RequiredArgsConstructor
public class InternoProductoController {

    private final ProductoService productoService;

    /** GET /internal/productos?ids=1,2,3 */
    @GetMapping
    public List<ProductoDto> porIds(@RequestParam List<Long> ids) {
        return productoService.porIds(ids);
    }
}
