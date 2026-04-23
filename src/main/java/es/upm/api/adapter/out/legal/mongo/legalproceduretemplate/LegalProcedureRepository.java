package es.upm.api.adapter.out.legal.mongo.legalproceduretemplate;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LegalProcedureRepository extends MongoRepository<LegalProcedureTemplateEntity, UUID> {
    @Query("{ 'title': { $regex: ?0, $options: 'i' } }")
    List<LegalProcedureTemplateEntity> searchByTitleContainingIgnoreCase(String title, Sort sort);

    Optional<LegalProcedureTemplateEntity> findByTitle(String title);
}