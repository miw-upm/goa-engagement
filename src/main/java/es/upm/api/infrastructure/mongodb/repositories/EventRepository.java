package es.upm.api.infrastructure.mongodb.repositories;

import es.upm.api.infrastructure.mongodb.entities.EventEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface EventRepository extends MongoRepository<EventEntity, UUID> {

    List<EventEntity> findByEngagementLetterIdOrderByEventDateAsc(UUID engagementLetterId);

    void deleteByEngagementLetterId(UUID engagementLetterId);
}
