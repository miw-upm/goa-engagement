package es.upm.api.infrastructure.mongodb.repositories;

import es.upm.api.infrastructure.mongodb.entities.ProcedimientoLegalEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProcedimientoLegalRepository extends MongoRepository<ProcedimientoLegalEntity, UUID> {
    @Query("{ 'titulo': { $regex: ?0, $options: 'i' } }")
    List<ProcedimientoLegalEntity> findByTituloContainingIgnoreCase(String titulo, Sort sort);

    Optional<ProcedimientoLegalEntity> findByTitulo(String titulo);
}