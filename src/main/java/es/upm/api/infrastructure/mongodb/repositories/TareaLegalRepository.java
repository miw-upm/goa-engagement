package es.upm.api.infrastructure.mongodb.repositories;

import es.upm.api.infrastructure.mongodb.entities.TareaLegalEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TareaLegalRepository extends MongoRepository<TareaLegalEntity, UUID> {
    @Query("{ 'titulo': { $regex: ?0, $options: 'i' } }")
    List<TareaLegalEntity> findByTituloContainingIgnoreCase(String titulo, Sort sort);

    Optional<TareaLegalEntity> findByTitulo(String titulo,  Sort sort);
}

