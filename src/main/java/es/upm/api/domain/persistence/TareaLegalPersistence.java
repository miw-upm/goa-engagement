package es.upm.api.domain.persistence;

import es.upm.api.domain.model.TareaLegal;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.stream.Stream;

@Repository
public interface TareaLegalPersistence {
    void create(TareaLegal tareaLegal);

    void deleteById(UUID id);

    TareaLegal read(UUID id);

    Stream<TareaLegal> findNullSafe(String titulo);

    Stream<TareaLegal> findAll();

    void update(UUID id, TareaLegal tareaLegal);
}
