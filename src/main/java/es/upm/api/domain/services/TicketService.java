package es.upm.api.domain.services;

import es.upm.api.domain.model.Ticket;
import es.upm.api.domain.persistence.TicketPersistence;
import es.upm.api.infrastructure.webclients.ArticleWebClient;
import es.upm.api.infrastructure.webclients.UserWebClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TicketService {
    private final TicketPersistence ticketPersistence;
    private final UserWebClient userWebClient;
    private final ArticleWebClient articleWebClient;

    @Autowired
    public TicketService(TicketPersistence ticketPersistence, UserWebClient userWebClient, ArticleWebClient articleWebClient) {
        this.ticketPersistence = ticketPersistence;
        this.userWebClient = userWebClient;
        this.articleWebClient = articleWebClient;
    }

    public Ticket readByUrlToken(String urlToken) {
        Ticket ticket = this.ticketPersistence.readByUrlToken(urlToken);
        ticket.setUserDto(this.userWebClient.readUserById(ticket.getUserDto().getId()));
        ticket.getTicketLines()
                .forEach(ticketLine ->
                        ticketLine.setArticleDto(this.articleWebClient.readArticleById(ticketLine.getArticleDto().getId()))
                );
        return ticket;
    }

}
