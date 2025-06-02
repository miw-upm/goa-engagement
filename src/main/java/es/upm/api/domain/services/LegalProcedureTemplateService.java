package es.upm.api.domain.services;

import es.upm.api.domain.model.LegalProcedureTemplate;
import es.upm.api.domain.persistence.LegalProcedureTemplatePersistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.stream.Stream;

@Service
public class LegalProcedureTemplateService {

    private final LegalProcedureTemplatePersistence legalProcedureTemplatePersistence;

    @Autowired
    public LegalProcedureTemplateService(LegalProcedureTemplatePersistence legalProcedureTemplatePersistence) {
        this.legalProcedureTemplatePersistence = legalProcedureTemplatePersistence;
    }

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

    public Stream<LegalProcedureTemplate> findNullSafe(String nombre) {
        return this.legalProcedureTemplatePersistence.findNullSafe(nombre);
    }

    public LegalProcedureTemplate readById(UUID id) {
        return this.legalProcedureTemplatePersistence.read(id);
    }

    public Stream<LegalProcedureTemplate> findAll() {
        return this.legalProcedureTemplatePersistence.findAll();
    }
}

