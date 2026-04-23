package es.upm.api.domain.model;

import es.upm.api.domain.model.snapshos.AccessLinkSnapshot;
import es.upm.api.domain.model.snapshos.UserSnapshot;
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
    private LocalDateTime signatureDate;
    private UserSnapshot signer;
    private AccessLinkSnapshot accessLinkSnapshot;
}

