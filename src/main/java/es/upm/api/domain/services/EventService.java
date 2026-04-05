package es.upm.api.domain.services;

import es.upm.api.domain.persistence.EventPersistence;
import org.springframework.stereotype.Service;
import es.upm.api.domain.model.Event;

import java.util.UUID;


@Service
public class EventService {
    private final EventPersistence eventPersistence;
    private final EngagementLetterService engagementLetterService;

    public EventService(EventPersistence eventPersistence, EngagementLetterService engagementLetterService) {
        this.eventPersistence = eventPersistence;
        this.engagementLetterService = engagementLetterService;

    }

    public Event create(Event event) {
        event.setId(UUID.randomUUID());
        this.engagementLetterService.readById(event.getEngagementLetterId());
        this.eventPersistence.create(event);
        return event;
    }

}
