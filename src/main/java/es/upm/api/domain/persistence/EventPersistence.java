package es.upm.api.domain.persistence;

import es.upm.api.domain.model.Event;
import org.springframework.stereotype.Repository;

@Repository
public interface EventPersistence {
    void create(Event event);
}
