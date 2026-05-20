package es.upm.api.adapter.in.legal.resources;

import es.upm.api.adapter.in.resources.EngagementLetterResource;
import es.upm.api.adapter.out.user.feign.GoaUserClient;
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

import static es.upm.api.configurations.DatabaseSeederDev.*;
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
    private GoaUserClient userFinderClient;

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_admin"})
    void testRead() throws Exception {
        BDDMockito.given(this.userFinderClient.readUserById(any(UUID.class)))
                .willAnswer(invocation -> mockedUser(invocation.getArgument(0)));
        mockMvc.perform(get(EngagementLetterResource.ENGAGEMENT_LETTER + EngagementLetterResource.ID_ID, ID_0))
                .andExpect(status().isOk());

    }

    private UserSnapshot mockedUser(UUID id) {
        if (C_0.equals(id)) {
            return UserSnapshot.builder().id(C_0).mobile("666666000").firstName("c1").familyName("family-c1")
                    .identity("66666603E").email("c1@gmail.com").build();
        }
        if (C_1.equals(id)) {
            return UserSnapshot.builder().id(C_1).mobile("666666001").firstName("c2").familyName("family-c2")
                    .identity("66666604T").email("c2@gmail.com").build();
        }
        if (C_2.equals(id)) {
            return UserSnapshot.builder().id(C_2).mobile("666666002").firstName("c3").familyName("family-c3")
                    .identity("66666605R").email("c3@gmail.com").build();
        }
        return UserSnapshot.builder().id(C_0).mobile("666666000").firstName("c1").familyName("family-c1")
                .identity("66666603E").email("c1@gmail.com").build();
    }
}
