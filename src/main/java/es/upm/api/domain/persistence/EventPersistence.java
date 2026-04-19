package es.upm.api.domain.persistence;

import es.upm.api.domain.model.Comment;
import es.upm.api.domain.model.Event;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.stream.Stream;

@Repository
public interface EventPersistence {
    void create(Event event);

    void delete(UUID id);

    void update(UUID id, Event event);

    Event readById(UUID id);

    Comment addComment(UUID eventId, Comment comment);

    void deleteComment(UUID eventId, Comment comment);

    Stream<Event> findByEngagementLetterId(UUID engagementLetterId);

    void deleteByEngagementLetterId(UUID engagementLetterId);
}
