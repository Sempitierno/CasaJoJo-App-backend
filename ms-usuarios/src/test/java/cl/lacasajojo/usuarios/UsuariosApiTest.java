package cl.lacasajojo.usuarios;

import cl.lacasajojo.common.modelo.Rol;
import cl.lacasajojo.common.seguridad.JwtService;
import cl.lacasajojo.common.seguridad.UsuarioAutenticado;
import cl.lacasajojo.usuarios.repository.UsuarioRepository;
import cl.lacasajojo.usuarios.service.PedidosClient;
import cl.lacasajojo.usuarios.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UsuariosApiTest {

    @Autowired MockMvc mvc;
    @Autowired UsuarioRepository repo;
    @Autowired UsuarioService service;
    @Autowired JwtService jwt;
    @MockitoBean PedidosClient pedidosClient;

    long adminId;
    String tokenAdmin;

    @BeforeEach
    void preparar() {
        repo.deleteAll();
        adminId = service.crear("Admin", "admin@x.cl", "secreta1", Rol.ADMINISTRADOR).id();
        tokenAdmin = jwt.generar(new UsuarioAutenticado(adminId, "admin@x.cl", Rol.ADMINISTRADOR));
    }

    static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder b, String body) {
        return b.contentType(MediaType.APPLICATION_JSON).content(body);
    }

    static String bearer(String t) { return "Bearer " + t; }

    static String campo(String json, String nombre) {
        Matcher m = Pattern.compile("\"" + nombre + "\"\\s*:\\s*\"?([^\",}]+)").matcher(json);
        if (!m.find()) throw new AssertionError(nombre + " no está en " + json);
        return m.group(1);
    }

    String login(String correo, String clave) throws Exception {
        String r = mvc.perform(json(post("/api/auth/login"), "{\"correo\":\"" + correo + "\",\"password\":\"" + clave + "\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return campo(r, "token");
    }

    @Test
    void registroPublicoCreaClienteYLoginEntregaToken() throws Exception {
        mvc.perform(json(post("/api/auth/registro"),
                        "{\"nombreCompleto\":\"Ana Pérez\",\"correo\":\" ANA@Correo.cl \",\"password\":\"secreta1\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("CLIENTE"))
                .andExpect(jsonPath("$.correo").value("ana@correo.cl"))
                .andExpect(jsonPath("$.claveHash").doesNotExist());

        mvc.perform(json(post("/api/auth/registro"),
                        "{\"nombreCompleto\":\"Otra\",\"correo\":\"ana@correo.cl\",\"password\":\"secreta1\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errores.correo").exists());

        String token = login("ana@correo.cl", "secreta1");
        mvc.perform(get("/api/auth/yo").header("Authorization", bearer(token)))
                .andExpect(jsonPath("$.nombreCompleto").value("Ana Pérez"));

        mvc.perform(json(post("/api/auth/login"), "{\"correo\":\"ana@correo.cl\",\"password\":\"mala\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Correo o contraseña incorrectos"));
    }

    @Test
    void soloAdminCreaPersonal() throws Exception {
        String cuerpo = "{\"nombreCompleto\":\"Chef\",\"correo\":\"chef@x.cl\",\"password\":\"secreta1\",\"rol\":\"COCINERO\"}";
        mvc.perform(json(post("/api/auth/registro"), cuerpo)).andExpect(status().isUnauthorized());
        mvc.perform(json(post("/api/usuarios"), cuerpo).header("Authorization", bearer(tokenAdmin)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.rol").value("COCINERO"));

        String tokenChef = login("chef@x.cl", "secreta1");
        mvc.perform(get("/api/usuarios").header("Authorization", bearer(tokenChef))).andExpect(status().isForbidden());
        mvc.perform(get("/api/usuarios?rol=COCINERO").header("Authorization", bearer(tokenAdmin)))
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void validacionesPorCampoYTokenInvalido() throws Exception {
        mvc.perform(json(post("/api/auth/registro"), "{\"nombreCompleto\":\"A\",\"correo\":\"nop\",\"password\":\"1\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nombreCompleto").exists())
                .andExpect(jsonPath("$.errores.correo").exists())
                .andExpect(jsonPath("$.errores.password").exists());
        mvc.perform(get("/api/auth/yo").header("Authorization", "Bearer basura")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/auth/yo")).andExpect(status().isUnauthorized());
    }

    @Test
    void perfilPasswordRecuperacionYRol() throws Exception {
        long id = service.crear("Ana", "ana@x.cl", "secreta1", Rol.CLIENTE).id();
        String token = login("ana@x.cl", "secreta1");

        mvc.perform(json(put("/api/usuarios/" + id), "{\"nombreCompleto\":\"Ana María\",\"correo\":\"ana.m@x.cl\"}")
                        .header("Authorization", bearer(token)))
                .andExpect(jsonPath("$.correo").value("ana.m@x.cl"));
        mvc.perform(json(put("/api/usuarios/" + adminId), "{\"nombreCompleto\":\"Hack\",\"correo\":\"h@x.cl\"}")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
        mvc.perform(json(patch("/api/usuarios/" + id + "/foto"), "{\"foto\":\"p.jpg\"}").header("Authorization", bearer(token)))
                .andExpect(jsonPath("$.fotoPerfil").value("p.jpg"));

        mvc.perform(json(put("/api/usuarios/" + id + "/password"), "{\"passwordActual\":\"mala\",\"nuevaPassword\":\"otra123\"}")
                .header("Authorization", bearer(token))).andExpect(status().isBadRequest());
        mvc.perform(json(put("/api/usuarios/" + id + "/password"), "{\"passwordActual\":\"secreta1\",\"nuevaPassword\":\"otra123\"}")
                .header("Authorization", bearer(token))).andExpect(status().isNoContent());

        String r = mvc.perform(json(post("/api/auth/recuperar"), "{\"correo\":\"ana.m@x.cl\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String codigo = campo(r, "codigoDemo");
        String incorrecto = codigo.equals("000000") ? "111111" : "000000";
        mvc.perform(json(post("/api/auth/restablecer"), "{\"correo\":\"ana.m@x.cl\",\"codigo\":\"" + incorrecto + "\",\"nuevaPassword\":\"nueva123\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errores.codigo").exists());
        mvc.perform(json(post("/api/auth/restablecer"), "{\"correo\":\"ana.m@x.cl\",\"codigo\":\"" + codigo + "\",\"nuevaPassword\":\"nueva123\"}"))
                .andExpect(status().isOk());
        login("ana.m@x.cl", "nueva123");

        mvc.perform(json(patch("/api/usuarios/" + id + "/rol"), "{\"rol\":\"CAJERO\"}").header("Authorization", bearer(tokenAdmin)))
                .andExpect(jsonPath("$.rol").value("CAJERO"));
        mvc.perform(json(patch("/api/usuarios/" + adminId + "/rol"), "{\"rol\":\"CLIENTE\"}").header("Authorization", bearer(tokenAdmin)))
                .andExpect(status().isConflict());
    }

    @Test
    void eliminarConsultaAMsPedidos() throws Exception {
        long id = service.crear("Ana", "ana@x.cl", "secreta1", Rol.CLIENTE).id();
        when(pedidosClient.clienteTienePedidos(anyLong())).thenReturn(true);
        mvc.perform(delete("/api/usuarios/" + id).header("Authorization", bearer(tokenAdmin))).andExpect(status().isConflict());
        when(pedidosClient.clienteTienePedidos(anyLong())).thenReturn(false);
        mvc.perform(delete("/api/usuarios/" + id).header("Authorization", bearer(tokenAdmin))).andExpect(status().isNoContent());
    }

    @Test
    void rutaInternaExigeClave() throws Exception {
        mvc.perform(get("/internal/usuarios/" + adminId)).andExpect(status().isForbidden());
        mvc.perform(get("/internal/usuarios/" + adminId).header("X-Internal-Key", "clave-interna-test"))
                .andExpect(jsonPath("$.correo").value("admin@x.cl"));
        mvc.perform(get("/salud")).andExpect(jsonPath("$.estado").value("OK"));
    }
}
