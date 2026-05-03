package es.upm.api.infrastructure.mongodb.entities;

import es.upm.api.domain.model.AcceptanceEvidence;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document
public class AcceptanceEvidenceEntity {
    @Id
    private UUID id;
    private UUID engagementLetterId;
    private LocalDateTime acceptedAt;
    private String ipAddress;
    private String userAgent;
    private String method;
    private UUID tokenId;

    public AcceptanceEvidenceEntity(AcceptanceEvidence acceptanceEvidence) {
        BeanUtils.copyProperties(acceptanceEvidence, this);
    }

    public AcceptanceEvidence toAcceptanceEvidence() {
        AcceptanceEvidence acceptanceEvidence = new AcceptanceEvidence();
        BeanUtils.copyProperties(this, acceptanceEvidence);
        return acceptanceEvidence;
    }
}
