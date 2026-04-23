package es.upm.api.domain.services;

import es.upm.api.domain.model.LegalProcedureTemplate;
import es.upm.api.domain.model.criteria.LegalProcedureTemplateFindCriteria;
import es.upm.api.domain.ports.out.legal.LegalProcedureTemplateGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class LegalProcedureTemplateService {

    private final LegalProcedureTemplateGateway legalProcedureTemplateGateway;

    public void create(LegalProcedureTemplate legalProcedureTemplate) {
        legalProcedureTemplate.setId(UUID.randomUUID());
        this.legalProcedureTemplateGateway.create(legalProcedureTemplate);
    }

    public void delete(UUID id) {
        this.legalProcedureTemplateGateway.deleteById(id);
    }

    public void update(UUID id, LegalProcedureTemplate legalProcedureTemplate) {
        this.legalProcedureTemplateGateway.update(id, legalProcedureTemplate);
    }

    public Stream<LegalProcedureTemplate> find(LegalProcedureTemplateFindCriteria criteria) {
        return this.legalProcedureTemplateGateway.find(criteria);
    }

    public LegalProcedureTemplate readById(UUID id) {
        return this.legalProcedureTemplateGateway.read(id);
    }

    public Stream<LegalProcedureTemplate> findAll() {
        return this.legalProcedureTemplateGateway.findAll();
    }
}

