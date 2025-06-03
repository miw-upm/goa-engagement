package es.upm.api.domain.persistence;

import es.upm.api.domain.model.EngagementLetter;
import jakarta.validation.Valid;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EngagementLetterPersistence {
    EngagementLetter readById(UUID id);

    void create(@Valid EngagementLetter engagementLetter);

    void delete(UUID id);

    void update(UUID id, EngagementLetter engagementLetter);
}
