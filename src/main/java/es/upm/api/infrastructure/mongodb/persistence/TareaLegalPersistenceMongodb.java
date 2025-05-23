package es.upm.api.infrastructure.mongodb.persistence;

import es.upm.api.domain.model.TareaLegal;
import es.upm.api.domain.persistence.TareaLegalPersistence;
import es.upm.api.infrastructure.mongodb.entities.TareaLegalEntity;
import es.upm.api.infrastructure.mongodb.repositories.TareaLegalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.stream.Stream;

@Repository
public class TareaLegalPersistenceMongodb implements TareaLegalPersistence {
    private final TareaLegalRepository tareaLegalRepository;

    @Autowired
    public TareaLegalPersistenceMongodb(TareaLegalRepository tareaLegalRepository) {
        this.tareaLegalRepository = tareaLegalRepository;
    }

    @Override
    public Stream<TareaLegal> findAll() {
        return this.tareaLegalRepository.findAll().stream()
                .map(TareaLegalEntity::toTareaLegal);
    }

    @Override
    public void create(TareaLegal tareaLegal) {
        if (this.tareaLegalRepository.findByTitulo(tareaLegal.getTitulo()).isEmpty()) {
            this.tareaLegalRepository.save(new TareaLegalEntity(tareaLegal));
        }
    }

    @Override
    public void deleteByTitulo(String titulo) {
        this.tareaLegalRepository.deleteByTitulo(titulo);
    }
}
