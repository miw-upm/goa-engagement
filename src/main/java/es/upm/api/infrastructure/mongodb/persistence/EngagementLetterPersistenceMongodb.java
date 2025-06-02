package es.upm.api.infrastructure.mongodb.persistence;

import es.upm.api.domain.exceptions.NotFoundException;
import es.upm.api.domain.model.EngagementLetter;
import es.upm.api.domain.persistence.EngagementLetterPersistence;
import es.upm.api.infrastructure.mongodb.repositories.EngagementLetterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class EngagementLetterPersistenceMongodb implements EngagementLetterPersistence {

    private final EngagementLetterRepository engagementLetterRepository;

    @Autowired
    public EngagementLetterPersistenceMongodb(EngagementLetterRepository engagementLetterRepository) {
        this.engagementLetterRepository = engagementLetterRepository;
    }

    @Override
    public EngagementLetter readById(UUID id) {
        return this.engagementLetterRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("The EngagementLetter ID doesn't exist: " + id))
                .toEngagementLetter();
    }
}

