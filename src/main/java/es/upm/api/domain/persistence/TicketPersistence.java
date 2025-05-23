package es.upm.api.domain.persistence;

import es.upm.api.domain.model.Ticket;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketPersistence {
    Ticket create(Ticket ticket);

    Ticket readByUrlToken(String urlToken);
}
