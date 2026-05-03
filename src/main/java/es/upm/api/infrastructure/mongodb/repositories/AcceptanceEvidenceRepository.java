package es.upm.api.infrastructure.mongodb.repositories;

import es.upm.api.infrastructure.mongodb.entities.AcceptanceEvidenceEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface AcceptanceEvidenceRepository extends MongoRepository<AcceptanceEvidenceEntity, UUID> {
}
