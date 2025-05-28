package es.upm.api.infrastructure.mongodb.persistence;

import es.upm.api.domain.exceptions.ConflictException;
import es.upm.api.domain.exceptions.NotFoundException;
import es.upm.api.domain.model.TareaLegal;
import es.upm.api.domain.persistence.TareaLegalPersistence;
import es.upm.api.infrastructure.mongodb.entities.TareaLegalEntity;
import es.upm.api.infrastructure.mongodb.repositories.TareaLegalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.stream.Stream;

@Repository
public class TareaLegalPersistenceMongodb implements TareaLegalPersistence {
    private final TareaLegalRepository tareaLegalRepository;

    @Autowired
    public TareaLegalPersistenceMongodb(TareaLegalRepository tareaLegalRepository) {
        this.tareaLegalRepository = tareaLegalRepository;
    }

    @Override
    public void create(TareaLegal tareaLegal) {
        assertNotExist(tareaLegal.getTitulo());
        tareaLegalRepository.save(new TareaLegalEntity(tareaLegal));
    }

    private void assertNotExist(String titulo) {
        if (tareaLegalRepository.findByTitulo(titulo, Sort.by(Sort.Direction.ASC, "titulo")).isPresent()) {
            throw new ConflictException("Ya existe una tarea legal con un título similar: " + titulo);
        }
    }

    @Override
    public void deleteById(UUID id) {
        this.tareaLegalRepository.deleteById(id);
    }

    @Override
    public TareaLegal read(UUID id) {
        return this.tareaLegalRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tarea legal no encontrada, id:" + id))
                .toTareaLegal();

    }

    @Override
    public Stream<TareaLegal> findNullSafe(String titulo) {
        if (titulo == null) {
            return this.findAll();
        } else {
            return this.tareaLegalRepository.findByTituloContainingIgnoreCase(titulo, Sort.by(Sort.Direction.ASC, "titulo")).stream()
                    .map(TareaLegalEntity::toTareaLegal);
        }

    }

    @Override
    public Stream<TareaLegal> findAll() {
        return this.tareaLegalRepository.findAll( Sort.by(Sort.Direction.ASC, "titulo")).stream()
                .map(TareaLegalEntity::toTareaLegal);
    }

    @Override
    public void update(UUID id, TareaLegal tareaLegal) {
        TareaLegalEntity tareaLegalEntityDb = this.tareaLegalRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tarea legal no encontrada, id:" + id));
        if (!tareaLegalEntityDb.getTitulo().equals(tareaLegal.getTitulo())) {
            this.assertNotExist(tareaLegal.getTitulo());
            tareaLegalEntityDb.setTitulo(tareaLegal.getTitulo());
            this.tareaLegalRepository.save(tareaLegalEntityDb);
        }
    }
}
