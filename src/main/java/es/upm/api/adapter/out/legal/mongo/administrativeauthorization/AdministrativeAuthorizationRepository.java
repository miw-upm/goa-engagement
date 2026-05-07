package es.upm.api.adapter.out.legal.mongo.administrativeauthorization;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface AdministrativeAuthorizationRepository extends MongoRepository<AdministrativeAuthorizationEntity, UUID> {
}
