package cl.lacasajojo.pedidos.client;

import cl.lacasajojo.common.interno.ClienteInterno;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/** Cliente HTTP hacia ms-productos: obtiene precios y disponibilidad vigentes. */
@Component
public class ProductosClient {

    private final RestClient rest;

    public ProductosClient(ClienteInterno clienteInterno, @Value("${app.servicios.productos}") String url) {
        this.rest = clienteInterno.para(url);
    }

    public List<ProductoRemoto> porIds(Collection<Long> ids) {
        if (ids.isEmpty()) return List.of();
        String lista = ids.stream().map(String::valueOf).collect(Collectors.joining(","));
        List<ProductoRemoto> r = rest.get().uri("/internal/productos?ids={ids}", lista)
                .retrieve().body(new ParameterizedTypeReference<List<ProductoRemoto>>() {});
        return r == null ? List.of() : r;
    }
}
