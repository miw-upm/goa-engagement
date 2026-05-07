package es.upm.api.domain.ports.out.legal;

import es.upm.api.domain.model.AdministrativeAuthorization;
import es.upm.api.domain.model.AdministrativeAuthorizationSignature;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AdministrativeAuthorizationGateway {
    void create(AdministrativeAuthorization administrativeAuthorization);

    AdministrativeAuthorization read(UUID id);

    void update(UUID id, AdministrativeAuthorization administrativeAuthorization);

    void delete(UUID id);

    void signWithToken(UUID id, AdministrativeAuthorizationSignature signature);
}
