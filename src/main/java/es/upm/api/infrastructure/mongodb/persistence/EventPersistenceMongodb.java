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
}
