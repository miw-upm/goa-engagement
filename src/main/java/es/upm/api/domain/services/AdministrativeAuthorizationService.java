package es.upm.api.domain.services;

import es.upm.api.domain.model.AdministrativeAuthorization;
import es.upm.api.domain.model.AdministrativeAuthorizationSignature;
import es.upm.api.domain.model.criteria.AdministrativeAuthorizationFindCriteria;
import es.upm.api.domain.model.external.AccessLinkSnapshot;
import es.upm.api.domain.model.external.UserSnapshot;
import es.upm.api.domain.ports.out.legal.AdministrativeAuthorizationGateway;
import es.upm.api.domain.ports.out.user.AccessLinkGateway;
import es.upm.api.domain.ports.out.user.UserFinder;
import es.upm.miw.exception.InvalidTransitionException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class AdministrativeAuthorizationService {
    private final AdministrativeAuthorizationGateway administrativeAuthorizationGateway;
    private final AccessLinkGateway accessLinkGateway;
    private final UserFinder userFinder;
    private final PasswordEncoder passwordEncoder;

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

    public Stream<AdministrativeAuthorization> find(AdministrativeAuthorizationFindCriteria criteria) {
        Stream<AdministrativeAuthorization> authorizations = this.administrativeAuthorizationGateway.find(criteria);
        if (StringUtils.hasText(criteria.getClient())) {
            List<UUID> clientIds = this.userFinder.find(criteria.getClient()).stream()
                    .map(UserSnapshot::getId)
                    .toList();
            authorizations = authorizations.filter(authorization -> authorization.isClientInAuthorization(clientIds));
        }
        return authorizations;
    }

    public Stream<UserSnapshot> findPendingSigners(UUID id) {
        AdministrativeAuthorization administrativeAuthorization = this.read(id);
        if (administrativeAuthorization.findPendingSigners().isEmpty()) {
            throw new InvalidTransitionException("Todos los clientes autorizantes ya han firmado");
        }
        return administrativeAuthorization.findPendingSigners().stream();
    }

    public void signWithToken(String scope, String urlId, String token, String signature) {
        AccessLinkSnapshot accessLink = this.accessLinkGateway.consume(scope, urlId, token);
        UserSnapshot user = this.userFinder.readByUrlIdWithToken(scope, urlId, token);
        AdministrativeAuthorization administrativeAuthorization = this.read(accessLink.getDocumentId());
        if (!administrativeAuthorization.isAuthorizingCustomer(user.getId())) {
            throw new InvalidTransitionException("El usuario no es un cliente autorizante de esta autorización administrativa");
        }
        AdministrativeAuthorizationSignature authorizationSignature = AdministrativeAuthorizationSignature.builder()
                .signedAt(LocalDateTime.now())
                .signerId(user.getId())
                .signerFullName(user.toFullName())
                .signatureToken(token)
                .signatureImage(this.decodeSignature(signature))
                .build();
        authorizationSignature.setSignatureToken(
                this.passwordEncoder.encode(authorizationSignature.getSignatureToken())
        );
        this.administrativeAuthorizationGateway.signWithToken(administrativeAuthorization.getId(), authorizationSignature);
    }

    private byte[] decodeSignature(String signature) {
        if (!StringUtils.hasText(signature)) {
            throw new InvalidTransitionException("La firma es obligatoria");
        }
        try {
            return Base64.getDecoder().decode(signature);
        } catch (IllegalArgumentException exception) {
            throw new InvalidTransitionException("El formato de la firma no es válido");
        }
    }

}
