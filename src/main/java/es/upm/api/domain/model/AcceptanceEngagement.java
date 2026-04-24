package es.upm.api.domain.model;

import es.upm.api.domain.model.external.UserSnapshot;
import es.upm.miw.device.DeviceInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AcceptanceEngagement {
    private LocalDateTime signatureAt;
    private UserSnapshot signer;
    private String signerFullName;
    private String signerIdentity;
    private String mobile;
    private String signerEmail;
    private String signatureToken;
    private DeviceInfo deviceInfo;

    public boolean isSigned() {
        return this.signatureAt != null
                && this.signer != null
                && this.signerFullName != null
                && this.signerIdentity != null
                && this.mobile != null
                && this.signerEmail != null
                && this.signatureToken != null
                && this.deviceInfo != null;
    }
}

