package es.upm.api.adapter.out.legal.mongo.administrativeauthorization;

import es.upm.api.domain.model.AdministrativeAuthorization;
import es.upm.api.domain.model.AdministrativeAuthorizationSignature;
import es.upm.api.domain.model.criteria.AdministrativeAuthorizationFindCriteria;
import es.upm.api.domain.ports.out.legal.AdministrativeAuthorizationGateway;
import es.upm.miw.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.UUID;
import java.util.stream.Stream;

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

    @Override
    public void signWithToken(UUID id, AdministrativeAuthorizationSignature signature) {
        AdministrativeAuthorization administrativeAuthorization = this.read(id);
        administrativeAuthorization.add(signature);
        this.administrativeAuthorizationRepository.save(
                new AdministrativeAuthorizationEntity(administrativeAuthorization)
        );
    }

    @Override
    public Stream<AdministrativeAuthorization> find(AdministrativeAuthorizationFindCriteria criteria) {
        Stream<AdministrativeAuthorization> administrativeAuthorizations = this.administrativeAuthorizationRepository
                .findAll(Sort.by(Sort.Direction.DESC, "lastUpdatedDate"))
                .stream()
                .map(AdministrativeAuthorizationEntity::toDomain);

        if (StringUtils.hasText(criteria.getAuthorizationPurpose())) {
            String purpose = criteria.getAuthorizationPurpose().toLowerCase();
            administrativeAuthorizations = administrativeAuthorizations
                    .filter(authorization -> authorization.getAuthorizationPurpose() != null &&
                            authorization.getAuthorizationPurpose().toLowerCase().contains(purpose));
        }
        if (criteria.getIsSigned() != null) {
            administrativeAuthorizations = administrativeAuthorizations
                    .filter(authorization -> criteria.getIsSigned().equals(authorization.isSigned()));
        }
        return administrativeAuthorizations;
    }
}
