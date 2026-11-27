package cl.lacasajojo.productos.controller;

import cl.lacasajojo.common.modelo.Rol;
import cl.lacasajojo.common.seguridad.Seguridad;
import cl.lacasajojo.productos.dto.CambioBooleanoRequest;
import cl.lacasajojo.productos.dto.ProductoDto;
import cl.lacasajojo.productos.dto.ProductoRequest;
import cl.lacasajojo.productos.service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    /**
     * Público: el menú publicado. Con ?publicados=false (menú completo, incluso ocultos) se exige personal.
     */
    @GetMapping
    public List<ProductoDto> listar(@RequestParam(defaultValue = "true") boolean publicados) {
        if (!publicados) Seguridad.requerirRol(Rol.ADMINISTRADOR, Rol.COCINERO, Rol.CAJERO);
        return productoService.listar(publicados);
    }

    @GetMapping("/{id}")
    public ProductoDto obtener(@PathVariable Long id) {
        return productoService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductoDto crear(@Valid @RequestBody ProductoRequest req) {
        Seguridad.requerirRol(Rol.ADMINISTRADOR);
        return productoService.crear(req);
    }

    @PutMapping("/{id}")
    public ProductoDto actualizar(@PathVariable Long id, @Valid @RequestBody ProductoRequest req) {
        Seguridad.requerirRol(Rol.ADMINISTRADOR);
        return productoService.actualizar(id, req);
    }

    @PatchMapping("/{id}/publicado")
    public ProductoDto cambiarPublicado(@PathVariable Long id, @Valid @RequestBody CambioBooleanoRequest req) {
        Seguridad.requerirRol(Rol.ADMINISTRADOR);
        return productoService.cambiarPublicado(id, req.valor());
    }

    /** Marcar agotado/disponible: administrador, cocinero o cajero. */
    @PatchMapping("/{id}/disponible")
    public ProductoDto cambiarDisponible(@PathVariable Long id, @Valid @RequestBody CambioBooleanoRequest req) {
        Seguridad.requerirRol(Rol.ADMINISTRADOR, Rol.COCINERO, Rol.CAJERO);
        return productoService.cambiarDisponible(id, req.valor());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        Seguridad.requerirRol(Rol.ADMINISTRADOR);
        productoService.eliminar(id);
    }
}
