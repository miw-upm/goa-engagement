package es.upm.api.infrastructure.mongodb.repositories;

import es.upm.api.infrastructure.mongodb.entities.LegalProcedureTemplateEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LegalProcedureRepository extends MongoRepository<LegalProcedureTemplateEntity, UUID> {
    @Query("{ 'title': { $regex: ?0, $options: 'i' } }")
    List<LegalProcedureTemplateEntity> findByTitleContainingIgnoreCase(String title, Sort sort);

    Optional<LegalProcedureTemplateEntity> findByTitle(String title);
}