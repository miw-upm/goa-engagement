package es.upm.api.domain.ports.out.legal;

import es.upm.api.domain.model.AuthorizationPurposeTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.stream.Stream;

@Repository
public interface AuthorizationPurposeTemplateGateway {
    void create(AuthorizationPurposeTemplate authorizationPurposeTemplate);

    void deleteById(UUID id);

    AuthorizationPurposeTemplate read(UUID id);

    Stream<AuthorizationPurposeTemplate> find(String purpose);

    Stream<AuthorizationPurposeTemplate> findAll();

    void update(UUID id, AuthorizationPurposeTemplate authorizationPurposeTemplate);
}
