package cl.lacasajojo.common.seguridad;

import cl.lacasajojo.common.modelo.Rol;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private final JwtService jwt = new JwtService("clave-de-pruebas-de-al-menos-32-caracteres-123", 8);

    @Test
    void generaYValida() {
        String token = jwt.generar(new UsuarioAutenticado(5L, "ana@x.cl", Rol.CAJERO));
        UsuarioAutenticado u = jwt.validar(token).orElseThrow();
        assertEquals(5L, u.id());
        assertEquals(Rol.CAJERO, u.rol());
    }

    @Test
    void rechazaTokenFirmadoConOtraClave() {
        JwtService otro = new JwtService("otra-clave-distinta-de-al-menos-32-caracteres", 8);
        String token = otro.generar(new UsuarioAutenticado(1L, "a@x.cl", Rol.ADMINISTRADOR));
        assertTrue(jwt.validar(token).isEmpty());
        assertTrue(jwt.validar("no-es-un-token").isEmpty());
    }

    @Test
    void exigeClaveLarga() {
        assertThrows(IllegalStateException.class, () -> new JwtService("corta", 8));
    }
}
