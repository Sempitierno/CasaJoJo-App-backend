package cl.lacasajojo.pedidos;

import cl.lacasajojo.common.modelo.CategoriaProducto;
import cl.lacasajojo.common.modelo.Rol;
import cl.lacasajojo.common.seguridad.JwtService;
import cl.lacasajojo.common.seguridad.UsuarioAutenticado;
import cl.lacasajojo.pedidos.client.ProductoRemoto;
import cl.lacasajojo.pedidos.client.ProductosClient;
import cl.lacasajojo.pedidos.client.UsuarioRemoto;
import cl.lacasajojo.pedidos.client.UsuariosClient;
import cl.lacasajojo.pedidos.repository.PedidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PedidosApiTest {

    @Autowired MockMvc mvc;
    @Autowired PedidoRepository repo;
    @Autowired JwtService jwt;
    @MockitoBean UsuariosClient usuariosClient;
    @MockitoBean ProductosClient productosClient;

    String admin, cocinero, cajero, cliente, otroCliente;
    final long retiro = System.currentTimeMillis() + 3_600_000L;

    @BeforeEach
    void preparar() {
        repo.deleteAll();
        admin = t(1L, Rol.ADMINISTRADOR);
        cocinero = t(2L, Rol.COCINERO);
        cajero = t(3L, Rol.CAJERO);
        cliente = t(10L, Rol.CLIENTE);
        otroCliente = t(11L, Rol.CLIENTE);
        when(usuariosClient.obtener(10L)).thenReturn(Optional.of(new UsuarioRemoto(10L, "Ana", "ana@x.cl", Rol.CLIENTE)));
        when(productosClient.porIds(anyCollection())).thenReturn(List.of(
                new ProductoRemoto(1L, "Pollo asado", CategoriaProducto.PLATO_FONDO, 4500, true, true),
                new ProductoRemoto(2L, "Jugo", CategoriaProducto.BEBIDA, 1500, true, true),
                new ProductoRemoto(3L, "Papas fritas", CategoriaProducto.ACOMPANAMIENTO, 1500, false, true)));
    }

    String t(Long id, Rol rol) {
        return "Bearer " + jwt.generar(new UsuarioAutenticado(id, "x@x.cl", rol));
    }

    static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder b, String token, String body) {
        return b.header("Authorization", token).contentType(MediaType.APPLICATION_JSON).content(body);
    }

    String crearPedido() throws Exception {
        String id = UUID.randomUUID().toString();
        mvc.perform(json(post("/api/pedidos"), cliente, """
                        {"id":"%s","seleccion":{"BEBIDA":2,"PLATO_FONDO":1},"especificaciones":" sin sal ","horaRetiro":%d}
                        """.formatted(id, retiro)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(6000))
                .andExpect(jsonPath("$.clienteId").value(10))
                .andExpect(jsonPath("$.clienteNombre").value("Ana"))
                .andExpect(jsonPath("$.especificaciones").value("sin sal"))
                .andExpect(jsonPath("$.detalles", hasSize(2)))
                .andExpect(jsonPath("$.detalles[0].categoria").value("PLATO_FONDO"));
        return id;
    }

    @Test
    void crearEsIdempotenteYValida() throws Exception {
        String id = crearPedido();
        mvc.perform(json(post("/api/pedidos"), cliente,
                        "{\"id\":\"" + id + "\",\"seleccion\":{\"PLATO_FONDO\":1},\"horaRetiro\":" + retiro + "}"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/pedidos").header("Authorization", admin)).andExpect(jsonPath("$", hasSize(1)));

        mvc.perform(json(post("/api/pedidos"), cliente,
                        "{\"id\":\"" + UUID.randomUUID() + "\",\"seleccion\":{\"BEBIDA\":2},\"horaRetiro\":1000}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.platoDeFondo").exists())
                .andExpect(jsonPath("$.errores.horaRetiro").exists());

        mvc.perform(json(post("/api/pedidos"), cliente,
                        "{\"id\":\"" + UUID.randomUUID() + "\",\"seleccion\":{\"PLATO_FONDO\":1,\"ACOMPANAMIENTO\":3},\"horaRetiro\":" + retiro + "}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje", containsString("Papas fritas")));

        mvc.perform(json(post("/api/pedidos"), cocinero,
                        "{\"id\":\"" + UUID.randomUUID() + "\",\"seleccion\":{\"PLATO_FONDO\":1},\"horaRetiro\":" + retiro + "}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/pedidos").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void clienteSoloVeLosSuyos() throws Exception {
        String id = crearPedido();
        mvc.perform(get("/api/pedidos").header("Authorization", cliente)).andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(get("/api/pedidos?clienteId=10").header("Authorization", otroCliente)).andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(get("/api/pedidos/" + id).header("Authorization", otroCliente)).andExpect(status().isNotFound());
        mvc.perform(get("/api/pedidos/" + id).header("Authorization", cajero)).andExpect(status().isOk());
    }

    @Test
    void flujoDeEstadosPorRol() throws Exception {
        String id = crearPedido();
        String en = "{\"estado\":\"%s\"}";
        mvc.perform(json(patch("/api/pedidos/" + id + "/estado"), cliente, en.formatted("EN_PREPARACION"))).andExpect(status().isForbidden());
        mvc.perform(json(patch("/api/pedidos/" + id + "/estado"), cajero, en.formatted("EN_PREPARACION"))).andExpect(status().isForbidden());
        mvc.perform(json(patch("/api/pedidos/" + id + "/estado"), cocinero, en.formatted("EN_PREPARACION")))
                .andExpect(jsonPath("$.estado").value("EN_PREPARACION"));

        mvc.perform(get("/api/pedidos/pendientes-notificar").header("Authorization", cliente)).andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(json(patch("/api/pedidos/" + id + "/notificado"), cliente, en.formatted("EN_PREPARACION"))).andExpect(status().isOk());
        mvc.perform(get("/api/pedidos/pendientes-notificar").header("Authorization", cliente)).andExpect(jsonPath("$", hasSize(0)));

        mvc.perform(json(patch("/api/pedidos/" + id + "/estado"), cocinero, en.formatted("ENTREGADO"))).andExpect(status().isForbidden());
        mvc.perform(json(patch("/api/pedidos/" + id + "/estado"), cocinero, en.formatted("LISTO_RETIRO"))).andExpect(status().isOk());
        mvc.perform(json(patch("/api/pedidos/" + id + "/foto"), cocinero, "{\"foto\":\"plato.jpg\"}"))
                .andExpect(jsonPath("$.fotoPedido").value("plato.jpg"));
        mvc.perform(json(patch("/api/pedidos/" + id + "/estado"), cajero, en.formatted("ENTREGADO")))
                .andExpect(jsonPath("$.estado").value("ENTREGADO"));
        mvc.perform(get("/api/pedidos?estado=ENTREGADO").header("Authorization", admin)).andExpect(jsonPath("$", hasSize(1)));

        mvc.perform(get("/internal/pedidos/existe?clienteId=10").header("X-Internal-Key", "clave-interna-test"))
                .andExpect(jsonPath("$.existe").value(true));
        mvc.perform(delete("/api/pedidos/" + id).header("Authorization", cocinero)).andExpect(status().isForbidden());
        mvc.perform(delete("/api/pedidos/" + id).header("Authorization", admin)).andExpect(status().isNoContent());
    }

    @Test
    void servicioCaidoDevuelve503() throws Exception {
        when(productosClient.porIds(anyCollection())).thenThrow(new ResourceAccessException("caído"));
        mvc.perform(json(post("/api/pedidos"), cliente,
                        "{\"id\":\"" + UUID.randomUUID() + "\",\"seleccion\":{\"PLATO_FONDO\":1},\"horaRetiro\":" + retiro + "}"))
                .andExpect(status().isServiceUnavailable());
    }
}
