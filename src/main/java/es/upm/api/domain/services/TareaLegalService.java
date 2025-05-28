package es.upm.api.domain.services;

import es.upm.api.domain.model.TareaLegal;
import es.upm.api.domain.persistence.TareaLegalPersistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.stream.Stream;

@Service
public class TareaLegalService {

    private final TareaLegalPersistence tareaLegalPersistence;

    @Autowired
    public TareaLegalService(TareaLegalPersistence tareaLegalPersistence) {
        this.tareaLegalPersistence = tareaLegalPersistence;
    }

    public void create(TareaLegal tareaLegal) {
        tareaLegal.setId(UUID.randomUUID());
        this.tareaLegalPersistence.create(tareaLegal);
    }

    public void deleteById(UUID id) {
        this.tareaLegalPersistence.deleteById(id);
    }

    public void update(UUID id, TareaLegal tareaLegal) {
        this.tareaLegalPersistence.update(id, tareaLegal);
    }

    public Stream<TareaLegal> findNullSafe(String titulo) {
        return this.tareaLegalPersistence.findNullSafe(titulo);
    }

    public Stream<TareaLegal> findAll() {
        return this.tareaLegalPersistence.findAll();
    }
}
