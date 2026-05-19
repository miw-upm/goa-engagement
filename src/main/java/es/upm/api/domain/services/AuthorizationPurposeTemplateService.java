package es.upm.api.domain.services;

import es.upm.api.domain.model.AuthorizationPurposeTemplate;
import es.upm.api.domain.ports.out.legal.AuthorizationPurposeTemplateGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class AuthorizationPurposeTemplateService {

    private final AuthorizationPurposeTemplateGateway authorizationPurposeTemplateGateway;

    public void create(AuthorizationPurposeTemplate authorizationPurposeTemplate) {
        authorizationPurposeTemplate.setId(UUID.randomUUID());
        this.authorizationPurposeTemplateGateway.create(authorizationPurposeTemplate);
    }

    public void update(UUID id, AuthorizationPurposeTemplate authorizationPurposeTemplate) {
        this.authorizationPurposeTemplateGateway.update(id, authorizationPurposeTemplate);
    }

    public AuthorizationPurposeTemplate read(UUID id) {
        return this.authorizationPurposeTemplateGateway.read(id);
    }

    public void deleteById(UUID id) {
        this.authorizationPurposeTemplateGateway.deleteById(id);
    }

    public Stream<AuthorizationPurposeTemplate> find(String purpose) {
        return this.authorizationPurposeTemplateGateway.find(purpose);
    }
}
