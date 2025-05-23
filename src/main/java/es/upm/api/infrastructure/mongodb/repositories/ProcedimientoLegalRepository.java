package es.upm.api.infrastructure.mongodb.repositories;

import es.upm.api.infrastructure.mongodb.entities.ProcedimientoLegalEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProcedimientoLegalRepository extends MongoRepository<ProcedimientoLegalEntity, UUID> {
    Optional<ProcedimientoLegalEntity> findByTitulo(String titulo);
}