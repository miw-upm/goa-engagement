package es.upm.api.infrastructure.resources;

import es.upm.api.domain.model.ArticleDto;
import es.upm.api.domain.model.UserDto;
import es.upm.api.infrastructure.webclients.ArticleWebClient;
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

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class TicketResourceIT {

    @Autowired
    TicketResource providerResource;

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private UserWebClient userWebClient;

    @MockitoBean
    private ArticleWebClient articleWebClient;

    @Test
    @WithMockUser(username = "admin", authorities = {"SCOPE_customer"})
    void testRead() throws Exception {
        BDDMockito.given(this.userWebClient.readUserById(any(UUID.class)))
                .willAnswer(invocation ->
                        UserDto.builder().id(invocation.getArgument(0)).mobile("666000666").firstName("mock").build());
        BDDMockito.given(this.articleWebClient.readArticleById(any(UUID.class)))
                .willAnswer(invocation ->
                        ArticleDto.builder().id(invocation.getArgument(0)).barcode("01234567891123")
                                .retailPrice(BigDecimal.ONE).description("mock").build());

        mockMvc.perform(get(TicketResource.TICKETS + TicketResource.URL_TOKEN, "AAAABBBBCCCCDDDDEEEE00"))
                .andExpect(status().isOk());
    }

}
