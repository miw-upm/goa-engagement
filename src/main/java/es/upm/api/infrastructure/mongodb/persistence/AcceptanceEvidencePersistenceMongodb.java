package es.upm.api.infrastructure.mongodb.persistence;

import es.upm.api.domain.model.AcceptanceEvidence;
import es.upm.api.domain.persistence.AcceptanceEvidencePersistence;
import es.upm.api.infrastructure.mongodb.entities.AcceptanceEvidenceEntity;
import es.upm.api.infrastructure.mongodb.repositories.AcceptanceEvidenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class AcceptanceEvidencePersistenceMongodb implements AcceptanceEvidencePersistence {
    private final AcceptanceEvidenceRepository acceptanceEvidenceRepository;

    @Autowired
    public AcceptanceEvidencePersistenceMongodb(AcceptanceEvidenceRepository acceptanceEvidenceRepository) {
        this.acceptanceEvidenceRepository = acceptanceEvidenceRepository;
    }

    @Override
    public AcceptanceEvidence create(AcceptanceEvidence acceptanceEvidence) {
        return this.acceptanceEvidenceRepository.save(new AcceptanceEvidenceEntity(acceptanceEvidence))
                .toAcceptanceEvidence();
    }
}
