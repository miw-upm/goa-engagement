package es.upm.api.domain.persistence;

import es.upm.api.domain.model.EngagementLetter;
import es.upm.api.domain.model.EngagementLetterFindCriteria;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.stream.Stream;

@Repository
public interface EngagementLetterPersistence {
    EngagementLetter readById(UUID id);

    void create(EngagementLetter engagementLetter);

    void delete(UUID id);

    void update(UUID id, EngagementLetter engagementLetter);

    Stream<EngagementLetter> findNullSafe(EngagementLetterFindCriteria criteria);
}
