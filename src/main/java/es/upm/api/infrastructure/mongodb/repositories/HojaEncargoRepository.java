package es.upm.api.infrastructure.mongodb.repositories;

import es.upm.api.infrastructure.mongodb.entities.HojaEncargoEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface HojaEncargoRepository extends MongoRepository<HojaEncargoEntity, UUID> {
    // Puedes agregar métodos de consulta adicionales si los necesitas
}

