package es.upm.api.infrastructure.mongodb.repositories;

import es.upm.api.infrastructure.mongodb.entities.TareaLegalEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

public interface TareaLegalRepository extends MongoRepository<TareaLegalEntity, UUID> {
    Optional<TareaLegalEntity> findByTitulo(String titulo);

    void deleteByTitulo(String titulo);
}

