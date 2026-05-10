package es.upm.api.domain.ports.out.legal;

import es.upm.api.domain.model.AdministrativeAuthorization;
import es.upm.api.domain.model.AdministrativeAuthorizationSignature;
import es.upm.api.domain.model.criteria.AdministrativeAuthorizationFindCriteria;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.stream.Stream;

@Repository
public interface AdministrativeAuthorizationGateway {
    void create(AdministrativeAuthorization administrativeAuthorization);

    AdministrativeAuthorization read(UUID id);

    void update(UUID id, AdministrativeAuthorization administrativeAuthorization);

    void delete(UUID id);

    void signWithToken(UUID id, AdministrativeAuthorizationSignature signature);

    Stream<AdministrativeAuthorization> find(AdministrativeAuthorizationFindCriteria criteria);
}
