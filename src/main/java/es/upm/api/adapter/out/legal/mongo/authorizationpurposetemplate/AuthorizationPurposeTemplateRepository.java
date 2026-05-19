package es.upm.api.adapter.out.legal.mongo.authorizationpurposetemplate;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuthorizationPurposeTemplateRepository extends MongoRepository<AuthorizationPurposeTemplateEntity, UUID> {
    @Query("{ 'title': { $regex: ?0, $options: 'i' } }")
    List<AuthorizationPurposeTemplateEntity> findByPurposeContainingIgnoreCase(String title, Sort sort);

    Optional<AuthorizationPurposeTemplateEntity> findByPurpose(String title);
}
