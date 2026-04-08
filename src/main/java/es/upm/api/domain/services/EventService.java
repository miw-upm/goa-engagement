package es.upm.api.domain.services;

import es.upm.api.domain.model.Comment;
import es.upm.api.domain.persistence.EventPersistence;
import es.upm.api.domain.model.Event;
import es.upm.api.domain.model.UserDto;
import es.upm.api.domain.webclients.UserWebClient;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;


@Service
public class EventService {
    private final EventPersistence eventPersistence;
    private final EngagementLetterService engagementLetterService;
    private final UserWebClient userWebClient;

    public EventService(EventPersistence eventPersistence,
                        EngagementLetterService engagementLetterService,
                        UserWebClient userWebClient) {
        this.eventPersistence = eventPersistence;
        this.engagementLetterService = engagementLetterService;
        this.userWebClient = userWebClient;
    }

    public Event create(Event event) {
        event.setId(UUID.randomUUID());
        event.setCreatedDate(LocalDateTime.now());
        this.engagementLetterService.readById(event.getEngagementLetterId());
        this.eventPersistence.create(event);
        return event;
    }

    public Comment addComment(UUID eventId, String authenticatedUser, String content) {
        UserDto author = this.userWebClient.readUserByMobile(authenticatedUser);
        Comment comment = Comment.builder()
                .author(author)
                .createdDate(LocalDateTime.now())
                .content(content)
                .build();
        return this.eventPersistence.addComment(eventId, comment);
    }

}
