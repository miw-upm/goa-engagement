package es.upm.api.adapter.out.legal.mongo.authorizationpurposetemplate;

import es.upm.api.domain.model.AuthorizationPurposeTemplate;
import es.upm.api.domain.ports.out.legal.AuthorizationPurposeTemplateGateway;
import es.upm.miw.exception.ConflictException;
import es.upm.miw.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.stream.Stream;

@Repository
@RequiredArgsConstructor
public class AuthorizationPurposeTemplateAdapter implements AuthorizationPurposeTemplateGateway {
    public static final Sort PURPOSE = Sort.by(Sort.Direction.ASC, "purpose");

    private final AuthorizationPurposeTemplateRepository authorizationPurposeTemplateRepository;

    @Override
    public void create(AuthorizationPurposeTemplate authorizationPurposeTemplate) {
        this.assertNotExist(authorizationPurposeTemplate.getPurpose());
        this.authorizationPurposeTemplateRepository.save(new AuthorizationPurposeTemplateEntity(authorizationPurposeTemplate));
    }

    private void assertNotExist(String purpose) {
        if (this.authorizationPurposeTemplateRepository.findByPurpose(purpose).isPresent()) {
            throw new ConflictException("An authorization purpose template with a similar purpose already exists: " + purpose);
        }
    }

    @Override
    public void deleteById(UUID id) {
        this.authorizationPurposeTemplateRepository.deleteById(id);
    }

    @Override
    public AuthorizationPurposeTemplate read(UUID id) {
        return this.authorizationPurposeTemplateRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Authorization purpose template not found, id:" + id))
                .toDomain();
    }

    @Override
    public Stream<AuthorizationPurposeTemplate> find(String purpose) {
        if (purpose == null) {
            return this.findAll();
        }
        return this.authorizationPurposeTemplateRepository.findByPurposeContainingIgnoreCase(purpose, PURPOSE).stream()
                .map(AuthorizationPurposeTemplateEntity::toDomain);
    }

    @Override
    public Stream<AuthorizationPurposeTemplate> findAll() {
        return this.authorizationPurposeTemplateRepository.findAll(PURPOSE).stream()
                .map(AuthorizationPurposeTemplateEntity::toDomain);
    }

    @Override
    public void update(UUID id, AuthorizationPurposeTemplate authorizationPurposeTemplate) {
        AuthorizationPurposeTemplateEntity authorizationPurposeTemplateEntityDb = this.authorizationPurposeTemplateRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Authorization purpose template not found, id:" + id));
        if (!authorizationPurposeTemplateEntityDb.getPurpose().equals(authorizationPurposeTemplate.getPurpose())) {
            this.assertNotExist(authorizationPurposeTemplate.getPurpose());
            authorizationPurposeTemplateEntityDb.setPurpose(authorizationPurposeTemplate.getPurpose());
            this.authorizationPurposeTemplateRepository.save(authorizationPurposeTemplateEntityDb);
        }
    }
}
