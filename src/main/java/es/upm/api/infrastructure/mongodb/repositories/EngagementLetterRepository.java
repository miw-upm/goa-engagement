package es.upm.api.infrastructure.mongodb.repositories;

import es.upm.api.domain.model.EngagementLetterFindCriteria;
import es.upm.api.infrastructure.mongodb.entities.EngagementLetterEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.UUID;

public interface EngagementLetterRepository extends MongoRepository<EngagementLetterEntity, UUID> {

    @Query("{ 'closingDate': null }")
    List<EngagementLetterEntity> findByOpened(EngagementLetterFindCriteria criteria);
}

