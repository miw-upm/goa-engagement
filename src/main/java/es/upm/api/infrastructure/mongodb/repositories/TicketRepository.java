package es.upm.api.infrastructure.mongodb.repositories;

import es.upm.api.infrastructure.mongodb.entities.TicketEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface TicketRepository extends MongoRepository<TicketEntity, String> {
    Optional<TicketEntity> findByUrlToken(String urlToken);
}
