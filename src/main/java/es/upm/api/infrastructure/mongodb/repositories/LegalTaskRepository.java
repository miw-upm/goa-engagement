package es.upm.api.infrastructure.mongodb.repositories;

import es.upm.api.infrastructure.mongodb.entities.LegalTaskEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LegalTaskRepository extends MongoRepository<LegalTaskEntity, UUID> {
    @Query("{ 'title': { $regex: ?0, $options: 'i' } }")
    List<LegalTaskEntity> findByTitleContainingIgnoreCase(String title, Sort sort);

    Optional<LegalTaskEntity> findByTitle(String title);
}

