package es.upm.api.adapter.in.legal.resources;

import es.upm.api.adapter.in.resources.AuthorizationPurposeTemplateResource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static es.upm.api.configurations.DatabaseSeederDev.UUIDS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class AuthorizationPurposeTemplateResourceIT {

    @Autowired
    AuthorizationPurposeTemplateResource authorizationPurposeTemplateResource;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_admin"})
    void testRead() throws Exception {
        this.mockMvc.perform(get(AuthorizationPurposeTemplateResource.AUTHORIZATION_PURPOSE_TEMPLATES
                + AuthorizationPurposeTemplateResource.ID_ID, UUIDS[0]))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.purpose").value("Gestiones bancarias y de seguros"));
    }
}
