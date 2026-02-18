package es.upm.api.infrastructure.mongodb.entities;

import es.upm.api.domain.model.AcceptanceEngagement;
import es.upm.api.domain.model.UserDto;
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
public class AcceptanceDocumentEntity {
    private LocalDateTime signatureDate;
    private UUID signer;
    private UUID accessLinkId;

    public AcceptanceDocumentEntity(AcceptanceEngagement acceptance) {
        BeanUtils.copyProperties(acceptance, this, "signer");
        if (acceptance.getSigner() != null) {
            this.signer = acceptance.getSigner().getId();
        }
    }

    public AcceptanceEngagement toAcceptanceDocument() {
        AcceptanceEngagement acceptance = new AcceptanceEngagement();
        BeanUtils.copyProperties(this, acceptance, "signer");
        acceptance.setSigner(UserDto.builder().id(this.signer).build());
        return acceptance;
    }
}
