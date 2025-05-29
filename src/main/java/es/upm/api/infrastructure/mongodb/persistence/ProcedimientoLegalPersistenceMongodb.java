package es.upm.api.infrastructure.mongodb.persistence;

import es.upm.api.domain.exceptions.NotFoundException;
import es.upm.api.domain.model.ProcedimientoLegal;
import es.upm.api.domain.persistence.ProcedimientoLegalPersistence;
import es.upm.api.infrastructure.mongodb.entities.ProcedimientoLegalEntity;
import es.upm.api.infrastructure.mongodb.repositories.ProcedimientoLegalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.UUID;
import java.util.stream.Stream;

@Repository
public class ProcedimientoLegalPersistenceMongodb implements ProcedimientoLegalPersistence {

    private final ProcedimientoLegalRepository repository;

    @Autowired
    public ProcedimientoLegalPersistenceMongodb(ProcedimientoLegalRepository repository) {
        this.repository = repository;
    }

    @Override
    public void create(ProcedimientoLegal procedimientoLegal) {
        this.repository.save(new ProcedimientoLegalEntity(procedimientoLegal));
    }

    @Override
    public void deleteById(UUID id) {
        if (!this.repository.existsById(id)) {
            throw new NotFoundException("ProcedimientoLegal id no encontrado: " + id);
        }
        this.repository.deleteById(id);
    }

    @Override
    public ProcedimientoLegal read(UUID id) {
        return this.repository.findById(id)
                .orElseThrow(() -> new NotFoundException("ProcedimientoLegal id no encontrado: " + id))
                .toProcedimientoLegal();

    }

    @Override
    public Stream<ProcedimientoLegal> findNullSafe(String titulo) {
        if (titulo == null || titulo.isBlank()) {
            return this.findAll();
        }
        return this.repository.findByTituloContainingIgnoreCase(titulo, Sort.by(Sort.Direction.ASC, "titulo"))
                .stream().map(ProcedimientoLegalEntity::toProcedimientoLegal);
    }

    @Override
    public Stream<ProcedimientoLegal> findAll() {
        return this.repository.findAll().stream().map(ProcedimientoLegalEntity::toProcedimientoLegal);
    }

    @Override
    public void update(UUID id, ProcedimientoLegal procedimientoLegal) {
        LocalDate fechaInicio = this.read(id).getFechaInicio();
        procedimientoLegal.setId(id);
        procedimientoLegal.setFechaInicio(fechaInicio);
        this.repository.save(new ProcedimientoLegalEntity(procedimientoLegal));
    }
}
