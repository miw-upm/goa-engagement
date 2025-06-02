package es.upm.api.domain.persistence;

import es.upm.api.domain.model.EngagementLetter;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EngagementLetterPersistence {
    EngagementLetter readById(UUID id);
}
