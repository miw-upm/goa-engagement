package es.upm.api.infrastructure.mongodb.persistence;

import es.upm.api.domain.exceptions.NotFoundException;
import es.upm.api.domain.model.Comment;
import es.upm.api.domain.model.Event;
import es.upm.api.domain.persistence.EventPersistence;
import es.upm.api.infrastructure.mongodb.entities.CommentEntity;
import es.upm.api.infrastructure.mongodb.entities.EventEntity;
import es.upm.api.infrastructure.mongodb.repositories.EventRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

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

    @Override
    public void delete(UUID id) {
        this.eventRepository.deleteById(id);
    }

    @Override
    public void update(UUID id, Event event) {
        Event eventBd = this.readById(id);
        event.setId(id);
        event.setCreatedDate(eventBd.getCreatedDate());
        event.setComments(eventBd.getComments());
        event.setEngagementLetterId(eventBd.getEngagementLetterId());
        this.eventRepository.save(new EventEntity(event));
    }


    @Override
    public Event readById(UUID id) {
        return this.eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("The Event ID doesn't exist: " + id))
                .toEvent();
    }

    @Override
    public Comment addComment(UUID eventId, Comment comment) {
        EventEntity eventEntity = this.eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("The Event ID doesn't exist: " + eventId));
        List<CommentEntity> comments = Optional.ofNullable(eventEntity.getComments())
                .map(ArrayList::new)
                .orElseGet(ArrayList::new);
        comments.add(new CommentEntity(comment));
        eventEntity.setComments(comments);
        this.eventRepository.save(eventEntity);
        return comment;
    }

    @Override
    public Stream<Event> findByEngagementLetterId(UUID engagementLetterId) {
        return this.eventRepository.findByEngagementLetterIdOrderByEventDateAsc(engagementLetterId).stream()
                .map(EventEntity::toEvent);
    }
}
