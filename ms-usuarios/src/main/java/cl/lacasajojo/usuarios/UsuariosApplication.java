package cl.lacasajojo.usuarios;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Microservicio de usuarios (puerto 8081): registro, login JWT, perfil, roles y recuperación. */
@SpringBootApplication(scanBasePackages = {"cl.lacasajojo.usuarios", "cl.lacasajojo.common"})
public class UsuariosApplication {
    public static void main(String[] args) {
        SpringApplication.run(UsuariosApplication.class, args);
    }
}
