package es.upm.api.domain.services;

import es.upm.api.domain.model.ProcedimientoLegal;
import es.upm.api.domain.persistence.ProcedimientoLegalPersistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.stream.Stream;

@Service
public class ProcedimientoLegalService {

    private final ProcedimientoLegalPersistence procedimientoLegalPersistence;

    @Autowired
    public ProcedimientoLegalService(ProcedimientoLegalPersistence procedimientoLegalPersistence) {
        this.procedimientoLegalPersistence = procedimientoLegalPersistence;
    }

    public void create(ProcedimientoLegal procedimientoLegal) {
        procedimientoLegal.setId(UUID.randomUUID());
        this.procedimientoLegalPersistence.create(procedimientoLegal);
    }

    public void delete(UUID id) {
        this.procedimientoLegalPersistence.deleteById(id);
    }

    public void update(UUID id, ProcedimientoLegal procedimientoLegal) {
        this.procedimientoLegalPersistence.update(id, procedimientoLegal);
    }

    public Stream<ProcedimientoLegal> findNullSafe(String nombre) {
        return this.procedimientoLegalPersistence.findNullSafe(nombre);
    }

    public ProcedimientoLegal readById(UUID id) {
        return this.procedimientoLegalPersistence.read(id);
    }

    public Stream<ProcedimientoLegal> findAll() {
        return this.procedimientoLegalPersistence.findAll();
    }
}

