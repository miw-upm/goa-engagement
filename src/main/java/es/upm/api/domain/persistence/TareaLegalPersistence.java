package es.upm.api.domain.persistence;

import es.upm.api.domain.model.TareaLegal;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.stream.Stream;

@Repository
public interface TareaLegalPersistence {
    Stream<TareaLegal> findAll();

    void create(TareaLegal tareaLegal);

    void deleteById(UUID id);

    TareaLegal read(UUID id);
}
