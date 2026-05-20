package es.upm.api.domain.services;

import es.upm.api.domain.model.AdministrativeAuthorization;
import es.upm.api.domain.model.AdministrativeAuthorizationSignature;
import es.upm.api.domain.model.criteria.AdministrativeAuthorizationFindCriteria;
import es.upm.api.domain.model.external.AccessLinkSnapshot;
import es.upm.api.domain.model.external.UserSnapshot;
import es.upm.api.domain.ports.out.legal.AdministrativeAuthorizationGateway;
import es.upm.api.domain.ports.out.user.AccessLinkGateway;
import es.upm.api.domain.ports.out.user.UserFinder;
import es.upm.api.domain.services.support.EncryptionService;
import es.upm.api.domain.services.support.HashService;
import es.upm.miw.exception.InvalidTransitionException;
import es.upm.miw.pdf.PdfBuilder;
import es.upm.miw.pdf.TextDictionary;
import lombok.RequiredArgsConstructor;
import org.openpdf.text.Element;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class AdministrativeAuthorizationService {
    private static final String SIGNATURE_PREFIX = "data:image/png;base64,";
    private final AdministrativeAuthorizationGateway administrativeAuthorizationGateway;
    private final AccessLinkGateway accessLinkGateway;
    private final UserFinder userFinder;
    private final HashService hashService;
    private final EncryptionService encryptionService;

    public void create(AdministrativeAuthorization administrativeAuthorization) {
        administrativeAuthorization.setId(UUID.randomUUID());
        administrativeAuthorization.setLastUpdatedDate(LocalDate.now());
        this.administrativeAuthorizationGateway.create(administrativeAuthorization);
    }

    public AdministrativeAuthorization read(UUID id) {
        AdministrativeAuthorization authorization = this.administrativeAuthorizationGateway.read(id);
        authorization.setAuthorizingCustomers(authorization.getAuthorizingCustomers().stream()
                .map(user -> this.userFinder.readById(user.getId())).toList()
        );
        authorization.setAuthorizedRepresentatives(authorization.getAuthorizedRepresentatives().stream()
                .map(user -> this.userFinder.readById(user.getId())).toList()
        );
        return authorization;
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
        return authorizations
                .map(auth -> {
                    auth.setAuthorizingCustomers(auth.getAuthorizingCustomers().stream()
                            .map(user -> this.userFinder.readById(user.getId())).toList()
                    );
                    auth.setAuthorizedRepresentatives(auth.getAuthorizedRepresentatives().stream()
                            .map(user -> this.userFinder.readById(user.getId())).toList()
                    );
                    return auth;
                });
    }

    public Stream<UserSnapshot> findPendingSigners(UUID id) {
        AdministrativeAuthorization administrativeAuthorization = this.read(id);
        if (administrativeAuthorization.findPendingSigners().isEmpty()) {
            throw new InvalidTransitionException("Todos los clientes autorizantes ya han firmado");
        }
        return administrativeAuthorization.findPendingSigners().stream();
    }

    public AdministrativeAuthorization readAuthorizationPurposeWithToken(String scope, String urlId, String token) {
        AccessLinkSnapshot accessLink = this.accessLinkGateway.consume(scope, urlId, token);
        UserSnapshot user = this.userFinder.readByUrlIdWithToken(scope, urlId, token);
        AdministrativeAuthorization administrativeAuthorization = this.read(accessLink.getDocumentId());
        if (administrativeAuthorization.isAuthorizingCustomer(user.getId())) {
            throw new InvalidTransitionException("El usuario no es un cliente autorizante de esta autorizaciÃ³n administrativa");
        }
        return administrativeAuthorization.ofPurpose();
    }

    public void signWithToken(String scope, String urlId, String token, String signature) {
        AccessLinkSnapshot accessLink = this.accessLinkGateway.consume(scope, urlId, token);
        UserSnapshot user = this.userFinder.readByUrlIdWithToken(scope, urlId, token);
        AdministrativeAuthorization administrativeAuthorization = this.read(accessLink.getDocumentId());
        if (administrativeAuthorization.isAuthorizingCustomer(user.getId())) {
            throw new InvalidTransitionException("El usuario no es un cliente autorizante de esta autorización administrativa");
        }
        AdministrativeAuthorizationSignature authorizationSignature = AdministrativeAuthorizationSignature.builder()
                .signedAt(LocalDateTime.now())
                .signerId(user.getId())
                .signerFullName(user.toFullName())
                .signatureToken(token)
                .signatureImage(this.encryptionService.encrypt(this.decodeSignature(signature)))
                .build();
        authorizationSignature.setSignatureToken(
                this.hashService.hash(authorizationSignature.getSignatureToken())
        );
        this.administrativeAuthorizationGateway.signWithToken(administrativeAuthorization.getId(), authorizationSignature);
    }

    private byte[] decodeSignature(String signature) {
        if (!StringUtils.hasText(signature)) {
            throw new InvalidTransitionException("La firma es obligatoria");
        }
        if (!signature.startsWith(SIGNATURE_PREFIX)) {
            throw new InvalidTransitionException("La firma debe tener formato data:image/png;base64,...");
        }
        try {
            return Base64.getDecoder().decode(signature.substring(SIGNATURE_PREFIX.length()));
        } catch (IllegalArgumentException exception) {
            throw new InvalidTransitionException("El formato de la firma no es válido");
        }
    }

    public byte[] generatePdf(UUID id) {
        AdministrativeAuthorization authorization = this.read(id);
        TextDictionary dict = new TextDictionary("templates/administrative-authorization-texts.yml");
        PdfBuilder pdf = new PdfBuilder()
                .space(6)
                .title(dict.getTitle("titulo"))
                .space(3)
                .paragraphBold(authorization.buildDate(), Element.ALIGN_RIGHT)
                .space(3)
                .paragraph(dict.getText("autorizante",
                        Map.of("autorizantes", authorization.buildCustomersFullNameIdentity()))
                )
                .space()
                .paragraph(dict.getText("autorizado",
                        Map.of("representantes", authorization.buildRepresentativesFullNameIdentity()))
                )
                .space()
                .paragraph(dict.getText("para",
                        Map.of("proposito", authorization.getPurpose()))
                )
                .space(2)
                .paragraph(dict.getText("firmas"));
        if (authorization.isSigned()) {
            List<PdfBuilder.LeftSignature> leftSignatures = authorization.getSignatures().stream()
                    .map(signature -> new PdfBuilder.LeftSignature(
                            signature.toDonFullName(),
                            null,
                            this.encryptionService.decrypt(signature.getSignatureImage())
                    ))
                    .toList();
            pdf.multiSignatureWithSignatures(leftSignatures);
        } else {
            List<PdfBuilder.LeftSignature> leftSignatures = authorization.getAuthorizingCustomers().stream()
                    .map(user ->
                            new PdfBuilder.LeftSignature(user.toDonFullName(), null, null)
                    ).toList();
            pdf.multiSignatureWithSignatures(leftSignatures);
        }
        return pdf.build();
    }
}
