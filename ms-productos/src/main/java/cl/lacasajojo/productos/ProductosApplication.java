package cl.lacasajojo.productos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Microservicio del menú del día (puerto 8082). */
@SpringBootApplication(scanBasePackages = {"cl.lacasajojo.productos", "cl.lacasajojo.common"})
public class ProductosApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProductosApplication.class, args);
    }
}
