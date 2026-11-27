package cl.lacasajojo.usuarios.config;

import cl.lacasajojo.common.modelo.Rol;
import cl.lacasajojo.usuarios.repository.UsuarioRepository;
import cl.lacasajojo.usuarios.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** Si no hay usuarios, crea uno de cada rol para la demostración (clave app.seed.password). */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;

    @Value("${app.seed.enabled:true}")
    private boolean habilitado;

    @Value("${app.seed.password:casajojo123}")
    private String password;

    @Override
    public void run(String... args) {
        if (!habilitado || usuarioRepository.count() > 0) return;
        usuarioService.crear("Administrador Casa JoJo", "admin@lacasajojo.cl", password, Rol.ADMINISTRADOR);
        usuarioService.crear("Cocinero Casa JoJo", "cocina@lacasajojo.cl", password, Rol.COCINERO);
        usuarioService.crear("Cajero Casa JoJo", "caja@lacasajojo.cl", password, Rol.CAJERO);
        usuarioService.crear("Cliente Demo", "cliente@lacasajojo.cl", password, Rol.CLIENTE);
        log.info("Usuarios de demostración creados (admin@, cocina@, caja@, cliente@lacasajojo.cl)");
    }
}
