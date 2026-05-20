package es.upm.api.domain.services.support;

import es.upm.api.domain.model.AdministrativeAuthorization;
import es.upm.api.domain.model.AdministrativeAuthorizationSignature;
import es.upm.api.domain.model.criteria.AdministrativeAuthorizationFindCriteria;
import es.upm.api.domain.ports.out.legal.AdministrativeAuthorizationGateway;
import es.upm.miw.exception.ConflictException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.Nullable;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@Log4j2
@Profile({"dev", "prod"})
public class SignatureMigrationOnStartup implements ApplicationRunner {
    private static final String LEGACY_PREFIX = "enc::";
    private final AdministrativeAuthorizationGateway administrativeAuthorizationGateway;
    private final EncryptionService encryptionService;
    private final BytesEncryptor legacyBytesEncryptor;

    public SignatureMigrationOnStartup(
            AdministrativeAuthorizationGateway administrativeAuthorizationGateway,
            EncryptionService encryptionService,
            @Qualifier("legacyBytesEncryptor") @Nullable BytesEncryptor legacyBytesEncryptor
    ) {
        this.administrativeAuthorizationGateway = administrativeAuthorizationGateway;
        this.encryptionService = encryptionService;
        if (legacyBytesEncryptor == null) {
            throw new ConflictException("No legacy key configured for rotation");
        }
        this.legacyBytesEncryptor = legacyBytesEncryptor;
    }

    @Override
    public void run(ApplicationArguments args) {
        this.rotateLegacyToCurrentPrefix();
    }

    public void rotateLegacyToCurrentPrefix() {
        log.warn("Starting legacy signature migration - this should only run during a controlled migration window");
        int authsProcessed = 0;
        int authsUpdated = 0;
        int signaturesRotated = 0;
        for (AdministrativeAuthorization authorization : this.administrativeAuthorizationGateway
                .find(new AdministrativeAuthorizationFindCriteria())
                .toList()) {
            authsProcessed++;
            int updated = this.rotateAuthorization(authorization);
            if (updated > 0) {
                authsUpdated++;
                signaturesRotated += updated;
            }
        }
        log.warn("Legacy signature migration completed - authorizations processed: {}, authorizations updated: {}, signatures rotated: {}",
                authsProcessed, authsUpdated, signaturesRotated);
    }

    private int rotateAuthorization(AdministrativeAuthorization authorization) {
        if (authorization.getSignatures() == null) {
            return 0;
        }
        int updated = 0;
        for (AdministrativeAuthorizationSignature signature : authorization.getSignatures()) {
            if (this.tryRotateSignature(signature)) {
                updated++;
            }
        }
        if (updated > 0) {
            log.warn("Re-encrypted legacy signatures - authorization id: {}, signatures rotated: {}", authorization.getId(), updated);
            this.administrativeAuthorizationGateway.update(authorization.getId(), authorization);
        }
        return updated;
    }

    private boolean tryRotateSignature(AdministrativeAuthorizationSignature signature) {
        byte[] image = signature.getSignatureImage();
        if (!LEGACY_PREFIX.equals(this.encryptionService.buildPrefix(image))) {
            return false;
        }
        byte[] legacyPayload = Arrays.copyOfRange(image, LEGACY_PREFIX.length(), image.length);
        byte[] newImage = this.encryptionService.encrypt(this.legacyBytesEncryptor.decrypt(legacyPayload));
        signature.setSignatureImage(newImage);
        return true;
    }
}
