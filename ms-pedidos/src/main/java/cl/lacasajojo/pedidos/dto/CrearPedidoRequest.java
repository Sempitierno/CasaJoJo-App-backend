package cl.lacasajojo.pedidos.dto;

import cl.lacasajojo.common.modelo.CategoriaProducto;
import jakarta.validation.constraints.*;

import java.util.Map;

/**
 * SolicitudPedido de la app. El cliente se toma del token (no del cuerpo). El backend vuelve a leer los
 * productos en ms-productos: usa los precios vigentes y rechaza los no publicados o no disponibles.
 */
public record CrearPedidoRequest(
        @NotBlank(message = "Falta el id del pedido")
        @Pattern(regexp = "^[0-9a-fA-F-]{36}$", message = "El id del pedido debe ser un UUID")
        String id,

        @NotEmpty(message = "Elige un plato de fondo para continuar")
        Map<CategoriaProducto, Long> seleccion,

        @Size(max = 300, message = "Máximo 300 caracteres")
        String especificaciones,

        @NotNull(message = "Elige la hora estimada de retiro")
        Long horaRetiro
) {}
