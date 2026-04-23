package es.upm.api.domain.services;

import es.upm.api.domain.model.LegalProcedureTemplate;
import es.upm.api.domain.model.criteria.LegalProcedureTemplateFindCriteria;
import es.upm.api.domain.persistence.LegalProcedureTemplatePersistence;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class LegalProcedureTemplateService {

    private final LegalProcedureTemplatePersistence legalProcedureTemplatePersistence;

    public void create(LegalProcedureTemplate legalProcedureTemplate) {
        legalProcedureTemplate.setId(UUID.randomUUID());
        this.legalProcedureTemplatePersistence.create(legalProcedureTemplate);
    }

    public void delete(UUID id) {
        this.legalProcedureTemplatePersistence.deleteById(id);
    }

    public void update(UUID id, LegalProcedureTemplate legalProcedureTemplate) {
        this.legalProcedureTemplatePersistence.update(id, legalProcedureTemplate);
    }

    public Stream<LegalProcedureTemplate> find(LegalProcedureTemplateFindCriteria criteria) {
        return this.legalProcedureTemplatePersistence.find(criteria);
    }

    public LegalProcedureTemplate readById(UUID id) {
        return this.legalProcedureTemplatePersistence.read(id);
    }

    public Stream<LegalProcedureTemplate> findAll() {
        return this.legalProcedureTemplatePersistence.findAll();
    }
}

