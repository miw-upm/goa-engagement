package es.upm.api.domain.services;

import es.upm.api.domain.model.ArticleDto;
import es.upm.api.domain.model.Ticket;
import es.upm.api.domain.model.UserDto;
import es.upm.api.infrastructure.webclients.ArticleWebClient;
import es.upm.api.infrastructure.webclients.UserWebClient;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
@ActiveProfiles("test")
class TicketServiceIT {

    @Autowired
    private TicketService ticketService;

    @MockitoBean
    private UserWebClient userWebClient;

    @MockitoBean
    private ArticleWebClient articleWebClient;

    @Test
    void testReadSuccess() {
        BDDMockito.given(this.userWebClient.readUserById(any(UUID.class)))
                .willAnswer(invocation ->
                        UserDto.builder().id(invocation.getArgument(0)).mobile("666000666").firstName("mock").build());
        BDDMockito.given(this.articleWebClient.readArticleById(any(UUID.class)))
                .willAnswer(invocation ->
                        ArticleDto.builder().id(invocation.getArgument(0)).barcode("01234567891123")
                                .retailPrice(BigDecimal.ONE).description("mock").build());

        Ticket ticket = ticketService.readByUrlToken("AAAABBBBCCCCDDDDEEEE00");
        assertThat(ticket)
                .isNotNull()
                .satisfies(retrieveTicket -> {
                    assertThat(retrieveTicket.getUserDto().getFirstName()).isEqualTo("mock");
                    assertThat(retrieveTicket.getUserDto().getMobile()).isEqualTo("666000666");
                    assertThat(retrieveTicket.getUrlToken()).isEqualTo("AAAABBBBCCCCDDDDEEEE00");
                });
    }

}
