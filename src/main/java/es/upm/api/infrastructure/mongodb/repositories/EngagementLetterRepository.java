package es.upm.api.infrastructure.mongodb.repositories;

import es.upm.api.infrastructure.mongodb.entities.EngagementLetterEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface EngagementLetterRepository extends MongoRepository<EngagementLetterEntity, UUID> {
}

