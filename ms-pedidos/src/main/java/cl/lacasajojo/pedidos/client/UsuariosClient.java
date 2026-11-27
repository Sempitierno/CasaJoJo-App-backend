package cl.lacasajojo.pedidos.client;

import cl.lacasajojo.common.interno.ClienteInterno;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Optional;

/** Cliente HTTP hacia ms-usuarios (comunicación entre microservicios). */
@Component
public class UsuariosClient {

    private final RestClient rest;

    public UsuariosClient(ClienteInterno clienteInterno, @Value("${app.servicios.usuarios}") String url) {
        this.rest = clienteInterno.para(url);
    }

    public Optional<UsuarioRemoto> obtener(Long id) {
        try {
            return Optional.ofNullable(rest.get().uri("/internal/usuarios/{id}", id).retrieve().body(UsuarioRemoto.class));
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) return Optional.empty();
            throw e;
        }
    }
}
