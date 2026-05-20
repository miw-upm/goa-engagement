package es.upm.api.domain.services.support;

import es.upm.api.domain.model.AdministrativeAuthorization;
import es.upm.api.domain.model.AdministrativeAuthorizationSignature;
import es.upm.api.domain.model.criteria.AdministrativeAuthorizationFindCriteria;
import es.upm.api.domain.ports.out.legal.AdministrativeAuthorizationGateway;
import es.upm.miw.exception.ConflictException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@Log4j2
@Profile({"dev", "prod"})
public class AdministrativeAuthorizationSignatureRotationService {
    private static final String LEGACY_PREFIX = "enc::";

    private final AdministrativeAuthorizationGateway administrativeAuthorizationGateway;
    private final EncryptionService encryptionService;
    private final BytesEncryptor legacyBytesEncryptor;

    public AdministrativeAuthorizationSignatureRotationService(
            AdministrativeAuthorizationGateway administrativeAuthorizationGateway,
            EncryptionService encryptionService,
            @Qualifier("legacyBytesEncryptor") BytesEncryptor legacyBytesEncryptor
    ) {
        this.administrativeAuthorizationGateway = administrativeAuthorizationGateway;
        this.encryptionService = encryptionService;
        if (legacyBytesEncryptor==null ){
            throw new ConflictException("No hay clave legacy configurada para rotacion");
        }
        this.legacyBytesEncryptor = legacyBytesEncryptor;
        this.rotateLegacyToCurrentPrefix();
    }

    public void  rotateLegacyToCurrentPrefix() {
        int updated = 0;
        List<AdministrativeAuthorization> authorizations = this.administrativeAuthorizationGateway
                .find(new AdministrativeAuthorizationFindCriteria())
                .toList();
        for (AdministrativeAuthorization authorization : authorizations) {
            if (authorization.getSignatures() == null) {
                continue;
            }
            for (AdministrativeAuthorizationSignature signature : authorization.getSignatures()) {
                byte[] image = signature.getSignatureImage();
                String prefix = this.encryptionService.buildPrefix(image);
                if (LEGACY_PREFIX.equals(prefix)) {
                    //TODO quitar a iamge el prefix;
                    byte[] newImageEncrypt = this.encryptionService.encrypt(this.legacyBytesEncryptor.decrypt(image));
                    signature.setSignatureImage(newImageEncrypt);
                    updated++;
                }
            }
            this.administrativeAuthorizationGateway.update(authorization.getId(), authorization);
        }
        log.warn("New encrypt for new version: " + updated);
    }

}
