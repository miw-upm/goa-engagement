package es.upm.api.domain.ports.out.legal;

import es.upm.api.domain.model.EngagementLetter;
import es.upm.api.domain.model.criteria.EngagementLetterFindCriteria;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.stream.Stream;

@Repository
public interface EngagementLetterGateway {
    EngagementLetter readById(UUID id);

    void create(EngagementLetter engagementLetter);

    void delete(UUID id);

    void update(UUID id, EngagementLetter engagementLetter);

    Stream<EngagementLetter> find(EngagementLetterFindCriteria criteria);
}
