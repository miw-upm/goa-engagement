package es.upm.api.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdministrativeAuthorizationSignature {
    private LocalDateTime signedAt;
    private UUID signerId;
    private String signerFullName;
    private String signatureToken;
    private byte[] signatureImage;
}
