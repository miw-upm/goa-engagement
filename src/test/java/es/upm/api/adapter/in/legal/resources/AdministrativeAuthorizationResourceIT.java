package es.upm.api.adapter.in.legal.resources;

import es.upm.api.adapter.in.resources.AdministrativeAuthorizationResource;
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

import static es.upm.api.configurations.DatabaseSeederDev.C_0;
import static es.upm.api.configurations.DatabaseSeederDev.C_1;
import static es.upm.api.configurations.DatabaseSeederDev.C_2;
import static es.upm.api.configurations.DatabaseSeederDev.ID_14;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class AdministrativeAuthorizationResourceIT {

    @Autowired
    AdministrativeAuthorizationResource administrativeAuthorizationResource;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GoaUserClient userFinderClient;

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_admin"})
    void testRead() throws Exception {
        BDDMockito.given(this.userFinderClient.readUserById(any(UUID.class)))
                .willAnswer(invocation -> mockedUser(invocation.getArgument(0)));

        this.mockMvc.perform(get(AdministrativeAuthorizationResource.ADMINISTRATIVE_AUTHORIZATION
                        + AdministrativeAuthorizationResource.ID_ID, ID_14))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ID_14.toString()))
                .andExpect(jsonPath("$.purpose").exists());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_admin"})
    void testFindPendingSigners() throws Exception {
        BDDMockito.given(this.userFinderClient.readUserById(any(UUID.class)))
                .willAnswer(invocation -> mockedUser(invocation.getArgument(0)));

        this.mockMvc.perform(get(AdministrativeAuthorizationResource.ADMINISTRATIVE_AUTHORIZATION
                        + AdministrativeAuthorizationResource.ID_ID
                        + AdministrativeAuthorizationResource.PENDING_SIGNERS, ID_14))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(C_0.toString()));
    }

    private UserSnapshot mockedUser(UUID id) {
        if (C_0.equals(id)) {
            return UserSnapshot.builder().id(C_0).mobile("600000100").firstName("cliente0").familyName("Garcia Lopez")
                    .identity("66666603E").email("c0@gmail.com").build();
        }
        if (C_1.equals(id)) {
            return UserSnapshot.builder().id(C_1).mobile("600000101").firstName("Manager1").familyName("Martinez Ruiz")
                    .identity("66666604T").email("m1@gmail.com").build();
        }
        if (C_2.equals(id)) {
            return UserSnapshot.builder().id(C_2).mobile("600000102").firstName("cliente2").familyName("Perez Diaz")
                    .identity("66666605R").email("c2@gmail.com").build();
        }
        return UserSnapshot.builder().id(C_0).mobile("600000100").firstName("cliente0").familyName("Garcia Lopez")
                .identity("66666603E").email("c0@gmail.com").build();
    }
}

