package es.upm.api.adapter.out.legal.mongo.administrativeauthorization;

import es.upm.api.domain.model.AdministrativeAuthorizationSignature;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdministrativeAuthorizationSignatureEntity {
    private LocalDateTime signedAt;
    private UUID signerId;
    private String signerFullName;
    private String signatureToken;
    private byte[] signatureImage;

    public AdministrativeAuthorizationSignatureEntity(AdministrativeAuthorizationSignature signature) {
        BeanUtils.copyProperties(signature, this);
    }

    public AdministrativeAuthorizationSignature toDomain() {
        AdministrativeAuthorizationSignature signature = new AdministrativeAuthorizationSignature();
        BeanUtils.copyProperties(this, signature);
        return signature;
    }
}
