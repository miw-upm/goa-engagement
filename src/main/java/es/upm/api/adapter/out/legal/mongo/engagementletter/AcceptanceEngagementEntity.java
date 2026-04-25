package es.upm.api.adapter.out.legal.mongo.engagementletter;

import es.upm.api.domain.model.AcceptanceEngagement;
import es.upm.miw.device.DeviceInfo;
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
public class AcceptanceEngagementEntity {
    private LocalDateTime signatureAt;
    private UUID signerId;
    private String signerFullName;
    private String signerIdentity;
    private String mobile;
    private String signerEmail;
    private String signatureToken;
    private DeviceInfo deviceInfo;
    private Boolean documentAccepted;

    public AcceptanceEngagementEntity(AcceptanceEngagement acceptance) {
        BeanUtils.copyProperties(acceptance, this);
    }

    public AcceptanceEngagement toDomain() {
        AcceptanceEngagement acceptance = new AcceptanceEngagement();
        BeanUtils.copyProperties(this, acceptance);
        return acceptance;
    }
}
