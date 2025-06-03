package es.upm.api.infrastructure.mongodb.persistence;

import es.upm.api.domain.exceptions.ConflictException;
import es.upm.api.domain.exceptions.NotFoundException;
import es.upm.api.domain.model.LegalProcedureTemplate;
import es.upm.api.domain.persistence.LegalProcedureTemplatePersistence;
import es.upm.api.infrastructure.mongodb.entities.LegalProcedureTemplateEntity;
import es.upm.api.infrastructure.mongodb.entities.LegalTaskEntity;
import es.upm.api.infrastructure.mongodb.repositories.LegalProcedureRepository;
import es.upm.api.infrastructure.mongodb.repositories.LegalTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Repository
public class LegalProcedureTemplatePersistenceMongodb implements LegalProcedureTemplatePersistence {

    private final LegalProcedureRepository procedureRepository;
    private final LegalTaskRepository taskRepository;

    @Autowired
    public LegalProcedureTemplatePersistenceMongodb(LegalProcedureRepository procedureRepository, LegalTaskRepository taskRepository) {
        this.procedureRepository = procedureRepository;
        this.taskRepository = taskRepository;
    }

    @Override
    public void create(LegalProcedureTemplate procedure) {
        this.assertNotExist(procedure.getTitle());
        LegalProcedureTemplateEntity entity = new LegalProcedureTemplateEntity(procedure);
        entity.setLegalTaskEntities(readTaskAssured(procedure));
        this.procedureRepository.save(entity);
    }

    private List<LegalTaskEntity> readTaskAssured(LegalProcedureTemplate procedure) {
        return procedure.getLegalTasks().stream()
                .map(legalTask -> this.taskRepository.findByTitle(legalTask.getTitle())
                        .orElseThrow(() -> new NotFoundException("Legal Task not found, title: " + legalTask.getTitle())))
                .toList();
    }

    private void assertNotExist(String title) {
        if (procedureRepository.findByTitle(title).isPresent()) {
            throw new ConflictException("A legal task with a similar title already exists: " + title);
        }
    }

    @Override
    public void deleteById(UUID id) {
        this.procedureRepository.deleteById(id);
    }

    @Override
    public LegalProcedureTemplate read(UUID id) {
        return this.procedureRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("LegalProcedureTemplate id not found: " + id))
                .toLegalProcedureTemplate();

    }

    @Override
    public Stream<LegalProcedureTemplate> findNullSafe(String title) {
        if (title == null || title.isBlank()) {
            return this.findAll();
        }
        return this.procedureRepository.findByTitleContainingIgnoreCase(title, Sort.by(Sort.Direction.ASC, "title"))
                .stream().map(LegalProcedureTemplateEntity::toLegalProcedureTemplate);
    }

    @Override
    public Stream<LegalProcedureTemplate> findAll() {
        return this.procedureRepository.findAll().stream().map(LegalProcedureTemplateEntity::toLegalProcedureTemplate);
    }

    @Override
    public void update(UUID id, LegalProcedureTemplate procedure) {
        LegalProcedureTemplate procedureDb = this.read(id);
        procedure.setId(id);
        if (!procedureDb.getTitle().equals(procedure.getTitle())) {
            this.assertNotExist(procedure.getTitle());
        }
        LegalProcedureTemplateEntity entity = new LegalProcedureTemplateEntity(procedure);
        entity.setLegalTaskEntities(readTaskAssured(procedure));
        this.procedureRepository.save(entity);
    }
}
