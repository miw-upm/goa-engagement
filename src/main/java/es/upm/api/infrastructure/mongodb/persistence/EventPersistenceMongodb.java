package es.upm.api.infrastructure.mongodb.persistence;

import es.upm.api.domain.model.Event;
import es.upm.api.domain.persistence.EventPersistence;
import es.upm.api.infrastructure.mongodb.entities.EventEntity;
import es.upm.api.infrastructure.mongodb.repositories.EventRepository;
import org.springframework.stereotype.Repository;

@Repository
public class EventPersistenceMongodb implements EventPersistence {

    private final EventRepository eventRepository;


    public EventPersistenceMongodb(EventRepository repository) {
        this.eventRepository = repository;
    }

    @Override
    public void create(Event event) {
        EventEntity eventEntity = new EventEntity(event);
        this.eventRepository.save(eventEntity);
    }
}
