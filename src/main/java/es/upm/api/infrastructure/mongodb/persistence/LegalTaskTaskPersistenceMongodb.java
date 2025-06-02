package es.upm.api.infrastructure.mongodb.persistence;

import es.upm.api.domain.exceptions.ConflictException;
import es.upm.api.domain.exceptions.NotFoundException;
import es.upm.api.domain.model.LegalTask;
import es.upm.api.domain.persistence.LegalTaskPersistence;
import es.upm.api.infrastructure.mongodb.entities.LegalTaskEntity;
import es.upm.api.infrastructure.mongodb.repositories.LegalTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.stream.Stream;

@Repository
public class LegalTaskTaskPersistenceMongodb implements LegalTaskPersistence {
    public static final Sort TITLE = Sort.by(Sort.Direction.ASC, "title");
    private final LegalTaskRepository legalTaskRepository;

    @Autowired
    public LegalTaskTaskPersistenceMongodb(LegalTaskRepository legalTaskRepository) {
        this.legalTaskRepository = legalTaskRepository;
    }

    @Override
    public void create(LegalTask legalTask) {
        assertNotExist(legalTask.getTitle());
        legalTaskRepository.save(new LegalTaskEntity(legalTask));
    }

    private void assertNotExist(String title) {
        if (legalTaskRepository.findByTitle(title, TITLE).isPresent()) {
            throw new ConflictException("A legal task with a similar title already exists: " + title);
        }
    }

    @Override
    public void deleteById(UUID id) {
        this.legalTaskRepository.deleteById(id);
    }

    @Override
    public LegalTask read(UUID id) {
        return this.legalTaskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Legal task not found, id:" + id))
                .toLegalTask();

    }

    @Override
    public Stream<LegalTask> findNullSafe(String title) {
        if (title == null) {
            return this.findAll();
        } else {
            return this.legalTaskRepository.findByTitleContainingIgnoreCase(title, TITLE).stream()
                    .map(LegalTaskEntity::toLegalTask);
        }

    }

    @Override
    public Stream<LegalTask> findAll() {
        return this.legalTaskRepository.findAll(TITLE).stream()
                .map(LegalTaskEntity::toLegalTask);
    }

    @Override
    public void update(UUID id, LegalTask legalTask) {
        LegalTaskEntity legalTaskEntityDb = this.legalTaskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Legal task not found, id, id:" + id));
        if (!legalTaskEntityDb.getTitle().equals(legalTask.getTitle())) {
            this.assertNotExist(legalTask.getTitle());
            legalTaskEntityDb.setTitle(legalTask.getTitle());
            this.legalTaskRepository.save(legalTaskEntityDb);
        }
    }
}
