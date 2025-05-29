package es.upm.api.domain.persistence;
import es.upm.api.domain.model.ProcedimientoLegal;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.stream.Stream;

@Repository
public interface ProcedimientoLegalPersistence {

    void create(ProcedimientoLegal procedimientoLegal);

    void deleteById(UUID id);

    ProcedimientoLegal read(UUID id);

    Stream<ProcedimientoLegal> findNullSafe(String nombre);

    Stream<ProcedimientoLegal> findAll();

    void update(UUID id, ProcedimientoLegal procedimientoLegal);
}

