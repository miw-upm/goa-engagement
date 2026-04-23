package es.upm.api.adapter.out.legal.mongo.engagementletter;


import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.UUID;

public interface EngagementLetterRepository extends MongoRepository<EngagementLetterEntity, UUID> {
    @Query("{'closingDate': null}")
    List<EngagementLetterEntity> findByOpened();

    @Query("{'closingDate': { $ne: null } }")
    List<EngagementLetterEntity> findByClosed();
}

