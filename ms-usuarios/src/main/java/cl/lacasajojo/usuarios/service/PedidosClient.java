package cl.lacasajojo.usuarios.service;

import cl.lacasajojo.common.interno.ClienteInterno;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

/** Llama a ms-pedidos para saber si un usuario tiene pedidos antes de eliminarlo. */
@Component
public class PedidosClient {

    private final RestClient rest;

    public PedidosClient(ClienteInterno clienteInterno, @Value("${app.servicios.pedidos}") String url) {
        this.rest = clienteInterno.para(url);
    }

    public boolean clienteTienePedidos(Long clienteId) {
        Map<?, ?> r = rest.get().uri("/internal/pedidos/existe?clienteId={id}", clienteId)
                .retrieve().body(Map.class);
        return r != null && Boolean.TRUE.equals(r.get("existe"));
    }
}
