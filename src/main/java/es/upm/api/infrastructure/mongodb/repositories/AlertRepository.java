package es.upm.api.infrastructure.mongodb.repositories;

import es.upm.api.infrastructure.mongodb.entities.AlertEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface AlertRepository extends MongoRepository<AlertEntity, UUID> {
    List<AlertEntity> findByEngagementLetterId(UUID engagementLetterId);
}
