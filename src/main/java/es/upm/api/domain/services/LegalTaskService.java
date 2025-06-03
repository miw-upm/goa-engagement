package es.upm.api.domain.services;

import es.upm.api.domain.model.LegalTask;
import es.upm.api.domain.persistence.LegalTaskPersistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.stream.Stream;

@Service
public class LegalTaskService {

    private final LegalTaskPersistence legalTaskPersistence;

    @Autowired
    public LegalTaskService(LegalTaskPersistence legalTaskPersistence) {
        this.legalTaskPersistence = legalTaskPersistence;
    }

    public void create(LegalTask legalTask) {
        legalTask.setId(UUID.randomUUID());
        this.legalTaskPersistence.create(legalTask);
    }

    public void deleteById(UUID id) {
        this.legalTaskPersistence.deleteById(id);
    }

    public void update(UUID id, LegalTask legalTask) {
        this.legalTaskPersistence.update(id, legalTask);
    }

    public Stream<LegalTask> findNullSafe(String title) {
        return this.legalTaskPersistence.findNullSafe(title);
    }

    public Stream<LegalTask> findAll() {
        return this.legalTaskPersistence.findAll();
    }

    public LegalTask read(UUID id) {
        return this.legalTaskPersistence.read(id);
    }
}
