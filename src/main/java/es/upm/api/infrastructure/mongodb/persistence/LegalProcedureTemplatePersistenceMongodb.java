package es.upm.api.infrastructure.mongodb.persistence;

import es.upm.api.domain.exceptions.NotFoundException;
import es.upm.api.domain.model.LegalProcedureTemplate;
import es.upm.api.domain.persistence.LegalProcedureTemplatePersistence;
import es.upm.api.infrastructure.mongodb.entities.LegalProcedureTemplateEntity;
import es.upm.api.infrastructure.mongodb.repositories.LegalProcedureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.stream.Stream;

@Repository
public class LegalProcedureTemplatePersistenceMongodb implements LegalProcedureTemplatePersistence {

    private final LegalProcedureRepository repository;

    @Autowired
    public LegalProcedureTemplatePersistenceMongodb(LegalProcedureRepository repository) {
        this.repository = repository;
    }

    @Override
    public void create(LegalProcedureTemplate legalProcedureTemplate) {
        this.repository.save(new LegalProcedureTemplateEntity(legalProcedureTemplate));
    }

    @Override
    public void deleteById(UUID id) {
        this.repository.deleteById(id);
    }

    @Override
    public LegalProcedureTemplate read(UUID id) {
        return this.repository.findById(id)
                .orElseThrow(() -> new NotFoundException("LegalProcedureTemplate id not found: " + id))
                .toLegalProcedureTemplate();

    }

    @Override
    public Stream<LegalProcedureTemplate> findNullSafe(String title) {
        if (title == null || title.isBlank()) {
            return this.findAll();
        }
        return this.repository.findByTitleContainingIgnoreCase(title, Sort.by(Sort.Direction.ASC, "title"))
                .stream().map(LegalProcedureTemplateEntity::toLegalProcedureTemplate);
    }

    @Override
    public Stream<LegalProcedureTemplate> findAll() {
        return this.repository.findAll().stream().map(LegalProcedureTemplateEntity::toLegalProcedureTemplate);
    }

    @Override
    public void update(UUID id, LegalProcedureTemplate legalProcedureTemplate) {
        this.read(id);
        legalProcedureTemplate.setId(id);
        this.repository.save(new LegalProcedureTemplateEntity(legalProcedureTemplate));
    }
}
