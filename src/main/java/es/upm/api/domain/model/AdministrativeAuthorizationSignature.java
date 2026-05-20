package es.upm.api.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import es.upm.api.domain.services.support.EncryptionService;
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
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private byte[] signatureImage;
    private String signatureImagePreview;

    public String getFormalDisplayName() {
        return "D./Dña. " + this.signerFullName;
    }

}
