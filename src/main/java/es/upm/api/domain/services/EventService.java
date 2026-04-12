package es.upm.api.domain.services;

import es.upm.api.domain.exceptions.ForbiddenException;
import es.upm.api.domain.exceptions.NotFoundException;
import es.upm.api.domain.model.Comment;
import es.upm.api.domain.model.Event;
import es.upm.api.domain.persistence.EventPersistence;
import es.upm.api.domain.webclients.UserWebClient;
import es.upm.api.infrastructure.dtos.EventUpdateDto;
import es.upm.api.infrastructure.mappers.EventMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Stream;


@Service
public class EventService {
    private final EventPersistence eventPersistence;
    private final EngagementLetterService engagementLetterService;
    private final UserWebClient userWebClient;
    private final EventMapper eventMapper;

    public EventService(EventPersistence eventPersistence,
                        EngagementLetterService engagementLetterService,
                        UserWebClient userWebClient,
                        EventMapper eventMapper) {
        this.eventPersistence = eventPersistence;
        this.engagementLetterService = engagementLetterService;
        this.userWebClient = userWebClient;
        this.eventMapper = eventMapper;
    }

    public Event readById(UUID id) {
        return this.eventPersistence.readById(id);
    }

    public Event create(Event event) {
        event.setId(UUID.randomUUID());
        event.setCreatedDate(LocalDateTime.now());
        if (event.getComments() == null) {
            event.setComments(new ArrayList<>());
        }
        this.engagementLetterService.readById(event.getEngagementLetterId());
        this.eventPersistence.create(event);
        return event;
    }

    public void delete(UUID id) {
        this.eventPersistence.delete(id);
    }

    public Event update(UUID id, EventUpdateDto eventUpdateDto) {
        Event existingEvent = this.eventPersistence.readById(id);
        Event updatedEvent = this.eventMapper.updateEntity(existingEvent, eventUpdateDto);
        this.eventPersistence.update(id, updatedEvent);
        return updatedEvent;
    }


    public Comment addComment(UUID eventId, String authenticatedUser, String content) {
        Comment comment = Comment.builder()
                .authorId(this.userWebClient.readUserByMobile(authenticatedUser).getId())
                .createdDate(LocalDateTime.now())
                .content(content)
                .build();
        return this.eventPersistence.addComment(eventId, comment);
    }

    public void deleteComment(UUID eventId, UUID commentAuthorId, LocalDateTime commentCreatedDate, String commentContent, String authenticatedUser) {
        Event event = this.eventPersistence.readById(eventId);

        UUID currentUserId = this.userWebClient.readUserByMobile(authenticatedUser).getId();

        Comment commentToDelete = event.getComments() != null ?
                event.getComments().stream()
                        .filter(comment -> comment.getAuthorId().equals(commentAuthorId) &&
                                comment.getCreatedDate().equals(commentCreatedDate) &&
                                comment.getContent().equals(commentContent))
                        .findFirst()
                        .orElse(null) : null;

        if (commentToDelete == null) {
            throw new NotFoundException("The comment doesn't exist in the event");
        }

        if (!commentToDelete.getAuthorId().equals(currentUserId)) {
            throw new ForbiddenException("You can only delete your own comments");
        }

        this.eventPersistence.deleteComment(eventId, commentToDelete);
    }

    public Stream<Event> findByEngagementLetterId(UUID engagementLetterId) {
        return this.eventPersistence.findByEngagementLetterId(engagementLetterId);
    }
}
