package es.upm.api.adapter.in.resources;

import es.upm.api.adapter.in.resources.dtos.AdministrativeAuthorizationSignatureCreationDto;
import es.upm.api.domain.model.AdministrativeAuthorization;
import es.upm.api.domain.services.AdministrativeAuthorizationService;
import es.upm.miw.security.Security;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@PreAuthorize(Security.ADMIN_MANAGER_OPERATOR)
@RestController
@RequestMapping(AdministrativeAuthorizationResource.ADMINISTRATIVE_AUTHORIZATION)
@RequiredArgsConstructor
public class AdministrativeAuthorizationResource {
    public static final String ADMINISTRATIVE_AUTHORIZATION = "/administrative-authorizations";
    public static final String ID_ID = "/{id}";
    public static final String SIGN_ADMINISTRATIVE_AUTHORIZATION = "/sign-administrative-authorization";
    public static final String URL_ID_TOKEN_ID = "/{urlId}/{token}";

    private final AdministrativeAuthorizationService administrativeAuthorizationService;

    @PostMapping
    public void create(@Valid @RequestBody AdministrativeAuthorization administrativeAuthorization) {
        this.administrativeAuthorizationService.create(administrativeAuthorization);
    }

    @GetMapping(ID_ID)
    public AdministrativeAuthorization read(@PathVariable UUID id) {
        return this.administrativeAuthorizationService.read(id);
    }

    @PutMapping(ID_ID)
    public void update(@PathVariable UUID id,
                       @Valid @RequestBody AdministrativeAuthorization administrativeAuthorization) {
        this.administrativeAuthorizationService.update(id, administrativeAuthorization);
    }

    @PreAuthorize(Security.ADMIN)
    @DeleteMapping(ID_ID)
    public void delete(@PathVariable UUID id) {
        this.administrativeAuthorizationService.delete(id);
    }

    @PreAuthorize(Security.ALL)
    @PatchMapping(SIGN_ADMINISTRATIVE_AUTHORIZATION + URL_ID_TOKEN_ID)
    public void signWithToken(@PathVariable String urlId, @PathVariable String token,
                              @Valid @RequestBody AdministrativeAuthorizationSignatureCreationDto signatureCreation) {
        this.administrativeAuthorizationService.signWithToken(
                SIGN_ADMINISTRATIVE_AUTHORIZATION.substring(1),
                urlId,
                token,
                signatureCreation.getSignature()
        );
    }
}
