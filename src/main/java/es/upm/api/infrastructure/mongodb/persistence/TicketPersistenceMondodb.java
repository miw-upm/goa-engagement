package es.upm.api.infrastructure.mongodb.persistence;

import es.upm.api.domain.exceptions.NotFoundException;
import es.upm.api.domain.model.Ticket;
import es.upm.api.domain.persistence.TicketPersistence;
import es.upm.api.infrastructure.mongodb.entities.TicketEntity;
import es.upm.api.infrastructure.mongodb.repositories.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class TicketPersistenceMondodb implements TicketPersistence {
    private final TicketRepository ticketRepository;

    @Autowired
    public TicketPersistenceMondodb(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Override
    public Ticket create(Ticket ticket) {
        return this.ticketRepository.save(new TicketEntity(ticket))
                .toTicket();
    }

    @Override
    public Ticket readByUrlToken(String urlToken) {
        return this.ticketRepository.findByUrlToken(urlToken)
                .orElseThrow(() -> new NotFoundException("The urlToken don't exist: " + urlToken))
                .toTicket();
    }
}
