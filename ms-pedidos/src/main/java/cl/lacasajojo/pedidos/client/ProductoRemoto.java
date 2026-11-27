package cl.lacasajojo.pedidos.client;

import cl.lacasajojo.common.modelo.CategoriaProducto;

/** Lo que ms-pedidos necesita de un producto de ms-productos. */
public record ProductoRemoto(Long id, String nombre, CategoriaProducto categoria, Integer precio,
                             boolean disponible, boolean publicado) {}
