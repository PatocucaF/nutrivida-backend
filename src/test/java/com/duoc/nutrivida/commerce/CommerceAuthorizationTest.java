package com.duoc.nutrivida.commerce;

import com.duoc.nutrivida.NutrividaApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = NutrividaApplication.class)
@AutoConfigureMockMvc
class CommerceAuthorizationTest {

    @Autowired private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "VENDEDOR")
    void vendedorPuedeConsultarCatalogoYListaDePedidos() throws Exception {
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/admin/orders"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/products?includeInactive=true"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "VENDEDOR")
    void vendedorNoPuedeAdministrarProductosInventarioVentasNiPedidos() throws Exception {
        String product = """
                {"sku":"TEST-1","name":"Producto de prueba","price":1000,
                 "initialStock":2,"categoryId":1,"active":true}
                """;
        String adjustment = """
                {"quantity":1,"type":"RESTOCK","note":"test"}
                """;

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON).content(product))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/inventory/products/1/adjustments")
                        .contentType(MediaType.APPLICATION_JSON).content(adjustment))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/sales"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/admin/orders/1"))
                .andExpect(status().isForbidden());
        mockMvc.perform(patch("/api/v1/admin/orders/1/cancel"))
                .andExpect(status().isForbidden());
    }

    @Test
    void clienteNoPuedeAccederAAdministracionDePedidos() throws Exception {
        mockMvc.perform(get("/api/v1/admin/orders").with(user("cliente").roles("CLIENTE")))
                .andExpect(status().isForbidden());
    }
}
