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
public class AcceptanceEvidence {
    private UUID id;
    private UUID engagementLetterId;
    private LocalDateTime acceptedAt;
    private String ipAddress;
    private String userAgent;
    private String method;
    private UUID tokenId;
}
