package cl.lacasajojo.common.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** GET /salud en cada microservicio para comprobar que está arriba. */
@RestController
public class SaludController {

    @Value("${spring.application.name}")
    private String servicio;

    @GetMapping({"/", "/salud"})
    public Map<String, String> salud() {
        return Map.of("estado", "OK", "servicio", servicio);
    }
}
