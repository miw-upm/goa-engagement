package es.upm.api.domain.ports.out.legal;

import es.upm.api.domain.model.LegalProcedureTemplate;
import es.upm.api.domain.model.criteria.LegalProcedureTemplateFindCriteria;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.stream.Stream;

@Repository
public interface LegalProcedureTemplateGateway {

    void create(LegalProcedureTemplate legalProcedureTemplate);

    void deleteById(UUID id);

    LegalProcedureTemplate read(UUID id);

    Stream<LegalProcedureTemplate> find(LegalProcedureTemplateFindCriteria criteria);

    Stream<LegalProcedureTemplate> findAll();

    void update(UUID id, LegalProcedureTemplate legalProcedureTemplate);
}

