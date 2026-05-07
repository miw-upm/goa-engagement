package es.upm.api.adapter.out.legal.mongo.administrativeauthorization;

import es.upm.api.domain.model.AdministrativeAuthorization;
import es.upm.api.domain.ports.out.legal.AdministrativeAuthorizationGateway;
import es.upm.miw.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AdministrativeAuthorizationAdapter implements AdministrativeAuthorizationGateway {
    private final AdministrativeAuthorizationRepository administrativeAuthorizationRepository;

    @Override
    public void create(AdministrativeAuthorization administrativeAuthorization) {
        this.administrativeAuthorizationRepository.save(
                new AdministrativeAuthorizationEntity(administrativeAuthorization)
        );
    }

    @Override
    public AdministrativeAuthorization read(UUID id) {
        return this.administrativeAuthorizationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("The AdministrativeAuthorization ID doesn't exist: " + id))
                .toDomain();
    }

    @Override
    public void update(UUID id, AdministrativeAuthorization administrativeAuthorization) {
        if (!id.equals(administrativeAuthorization.getId()) || !this.administrativeAuthorizationRepository.existsById(id)) {
            throw new NotFoundException("For update The AdministrativeAuthorization must exist: " + id);
        }
        this.administrativeAuthorizationRepository.save(
                new AdministrativeAuthorizationEntity(administrativeAuthorization)
        );
    }

    @Override
    public void delete(UUID id) {
        this.administrativeAuthorizationRepository.deleteById(id);
    }
}
