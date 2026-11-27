package cl.lacasajojo.pedidos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Microservicio de pedidos (puerto 8083). Consulta a ms-usuarios y ms-productos. */
@SpringBootApplication(scanBasePackages = {"cl.lacasajojo.pedidos", "cl.lacasajojo.common"})
public class PedidosApplication {
    public static void main(String[] args) {
        SpringApplication.run(PedidosApplication.class, args);
    }
}
