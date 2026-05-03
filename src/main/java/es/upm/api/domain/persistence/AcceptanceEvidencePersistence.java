package es.upm.api.domain.persistence;

import es.upm.api.domain.model.AcceptanceEvidence;
import org.springframework.stereotype.Repository;

@Repository
public interface AcceptanceEvidencePersistence {
    AcceptanceEvidence create(AcceptanceEvidence acceptanceEvidence);
}
