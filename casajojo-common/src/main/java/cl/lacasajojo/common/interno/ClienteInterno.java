package cl.lacasajojo.common.interno;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/** Crea RestClient para llamar a otro microservicio con la clave interna ya puesta. */
@Component
public class ClienteInterno {

    private final RestClient.Builder builder;
    private final String clave;

    public ClienteInterno(RestClient.Builder builder, @Value("${app.interno.clave}") String clave) {
        this.builder = builder;
        this.clave = clave;
    }

    public RestClient para(String urlBase) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(Duration.ofSeconds(3));
        fabrica.setReadTimeout(Duration.ofSeconds(5));
        return builder.clone()
                .baseUrl(urlBase)
                .requestFactory(fabrica)
                .defaultHeader(InternoFilter.HEADER, clave)
                .build();
    }
}
