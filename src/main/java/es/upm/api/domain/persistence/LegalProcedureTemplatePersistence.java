package es.upm.api.domain.persistence;

import es.upm.api.domain.model.LegalProcedureTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.stream.Stream;

@Repository
public interface LegalProcedureTemplatePersistence {

    void create(LegalProcedureTemplate legalProcedureTemplate);

    void deleteById(UUID id);

    LegalProcedureTemplate read(UUID id);

    Stream<LegalProcedureTemplate> findNullSafe(String nombre);

    Stream<LegalProcedureTemplate> findAll();

    void update(UUID id, LegalProcedureTemplate legalProcedureTemplate);
}

