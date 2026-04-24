package es.upm.api.adapter.in.legal.resources;

import es.upm.api.adapter.in.resources.EngagementLetterResource;
import es.upm.api.adapter.out.user.feign.UserFinderClient;
import es.upm.api.domain.model.external.UserSnapshot;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static es.upm.api.configurations.DatabaseSeederDev.UUIDS;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class EngagementLetterResourceIT {

    @Autowired
    EngagementLetterResource providerResource;

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private UserFinderClient userFinderClient;

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_admin"})
    void testRead() throws Exception {
        BDDMockito.given(this.userFinderClient.readUserById(any(UUID.class)))
                .willAnswer(invocation ->
                        UserSnapshot.builder().id(invocation.getArgument(0)).mobile("666000666").firstName("mock").build());
        mockMvc.perform(get(EngagementLetterResource.ENGAGEMENT_LETTER + EngagementLetterResource.ID_ID, UUIDS[0]))
                .andExpect(status().isOk());

    }
}
