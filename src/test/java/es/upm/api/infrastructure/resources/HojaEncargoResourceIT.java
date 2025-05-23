package es.upm.api.infrastructure.resources;

import es.upm.api.domain.model.UserDto;
import es.upm.api.infrastructure.webclients.UserWebClient;
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

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class HojaEncargoResourceIT {

    @Autowired
    HojaEncargoResource providerResource;

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private UserWebClient userWebClient;

    @Test
    @WithMockUser(username = "admin", authorities = {"SCOPE_customer"})
    void testRead() throws Exception {
        BDDMockito.given(this.userWebClient.readUserById(any(UUID.class)))
                .willAnswer(invocation ->
                        UserDto.builder().id(invocation.getArgument(0)).mobile("666000666").firstName("mock").build());
        mockMvc.perform(get(HojaEncargoResource.HOJAS_ENCARGO + HojaEncargoResource.ID_ID, "aaaaaaa0-bbbb-cccc-dddd-eeeeffff0000"))
                .andExpect(status().isOk());

    }
}
