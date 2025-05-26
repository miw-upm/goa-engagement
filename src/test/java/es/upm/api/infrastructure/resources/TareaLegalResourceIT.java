package es.upm.api.infrastructure.resources;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class TareaLegalResourceIT {

    @Autowired
    TareaLegalResource tareaLegalResource;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_admin"})
    void testRead() throws Exception {
        mockMvc.perform(get(TareaLegalResource.TAREAS_LEGALES + TareaLegalResource.TITULOS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tareasLegales", hasItem("Estudio de antecedentes y documentación")));
    }

}
