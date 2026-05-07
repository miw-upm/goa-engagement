package es.upm.api.domain.services;

import es.upm.api.domain.model.AdministrativeAuthorization;
import es.upm.api.domain.ports.out.legal.AdministrativeAuthorizationGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdministrativeAuthorizationService {
    private final AdministrativeAuthorizationGateway administrativeAuthorizationGateway;

    public void create(AdministrativeAuthorization administrativeAuthorization) {
        administrativeAuthorization.setId(UUID.randomUUID());
        administrativeAuthorization.setLastUpdatedDate(LocalDate.now());
        this.administrativeAuthorizationGateway.create(administrativeAuthorization);
    }

    public AdministrativeAuthorization read(UUID id) {
        return this.administrativeAuthorizationGateway.read(id);
    }

    public void update(UUID id, AdministrativeAuthorization administrativeAuthorization) {
        administrativeAuthorization.setId(id);
        administrativeAuthorization.setLastUpdatedDate(LocalDate.now());
        this.administrativeAuthorizationGateway.update(id, administrativeAuthorization);
    }

    public void delete(UUID id) {
        this.administrativeAuthorizationGateway.delete(id);
    }
}
