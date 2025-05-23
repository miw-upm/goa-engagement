package es.upm.api.domain.services;

import es.upm.api.domain.model.TareaLegal;
import es.upm.api.domain.persistence.TareaLegalPersistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

@Service
public class TareaLegalService {

    private final TareaLegalPersistence tareaLegalPersistence;

    @Autowired
    public TareaLegalService(TareaLegalPersistence tareaLegalPersistence) {
        this.tareaLegalPersistence = tareaLegalPersistence;
    }


    public Stream<TareaLegal> findAll() {
        return this.tareaLegalPersistence.findAll();
    }

    public void create(TareaLegal tareaLegal) {
        this.tareaLegalPersistence.create(tareaLegal);
    }

    public void deleteByTitulo(String titulo) {
        this.tareaLegalPersistence.deleteByTitulo(titulo);
    }
}
