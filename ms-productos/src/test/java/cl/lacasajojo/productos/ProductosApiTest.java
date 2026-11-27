package cl.lacasajojo.productos;

import cl.lacasajojo.common.modelo.Rol;
import cl.lacasajojo.common.seguridad.JwtService;
import cl.lacasajojo.common.seguridad.UsuarioAutenticado;
import cl.lacasajojo.productos.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProductosApiTest {

    @Autowired MockMvc mvc;
    @Autowired ProductoRepository repo;
    @Autowired JwtService jwt;

    String admin, cocinero, cliente;

    @BeforeEach
    void preparar() {
        repo.deleteAll();
        admin = "Bearer " + jwt.generar(new UsuarioAutenticado(1L, "a@x.cl", Rol.ADMINISTRADOR));
        cocinero = "Bearer " + jwt.generar(new UsuarioAutenticado(2L, "c@x.cl", Rol.COCINERO));
        cliente = "Bearer " + jwt.generar(new UsuarioAutenticado(3L, "u@x.cl", Rol.CLIENTE));
    }

    long crear(String body) throws Exception {
        String r = mvc.perform(post("/api/productos").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        Matcher m = Pattern.compile("\"id\":(\\d+)").matcher(r);
        m.find();
        return Long.parseLong(m.group(1));
    }

    @Test
    void crudYPermisos() throws Exception {
        String body = "{\"nombre\":\" Cazuela \",\"categoria\":\"PLATO_FONDO\",\"precio\":5000}";
        mvc.perform(post("/api/productos").header("Authorization", cliente).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());
        long id = crear(body);
        crear("{\"nombre\":\"Agua\",\"categoria\":\"BEBIDA\",\"precio\":1000}");

        mvc.perform(get("/api/productos")).andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].categoria").value("PLATO_FONDO"))
                .andExpect(jsonPath("$[0].nombre").value("Cazuela"));

        mvc.perform(put("/api/productos/" + id).header("Authorization", admin).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Cazuela\",\"categoria\":\"PLATO_FONDO\",\"precio\":5500,\"publicado\":false}"))
                .andExpect(jsonPath("$.precio").value(5500));
        mvc.perform(get("/api/productos")).andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(get("/api/productos?publicados=false")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/productos?publicados=false").header("Authorization", cocinero)).andExpect(jsonPath("$", hasSize(2)));

        mvc.perform(patch("/api/productos/" + id + "/publicado").header("Authorization", cocinero)
                .contentType(MediaType.APPLICATION_JSON).content("{\"valor\":true}")).andExpect(status().isForbidden());
        mvc.perform(patch("/api/productos/" + id + "/disponible").header("Authorization", cocinero)
                .contentType(MediaType.APPLICATION_JSON).content("{\"valor\":false}")).andExpect(jsonPath("$.disponible").value(false));

        mvc.perform(get("/internal/productos?ids=" + id).header("X-Internal-Key", "clave-interna-test"))
                .andExpect(jsonPath("$", hasSize(1)));

        mvc.perform(delete("/api/productos/" + id).header("Authorization", admin)).andExpect(status().isNoContent());
        mvc.perform(get("/api/productos/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void validaciones() throws Exception {
        mvc.perform(post("/api/productos").header("Authorization", admin).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"\",\"precio\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nombre").exists())
                .andExpect(jsonPath("$.errores.categoria").exists())
                .andExpect(jsonPath("$.errores.precio").exists());
    }
}
