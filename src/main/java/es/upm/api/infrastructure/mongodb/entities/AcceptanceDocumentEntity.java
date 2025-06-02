package es.upm.api.infrastructure.mongodb.entities;

import es.upm.api.domain.model.AcceptanceDocument;
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
    private String receipt;
    private UUID signer;

    public AcceptanceDocumentEntity(AcceptanceDocument acceptance) {
        BeanUtils.copyProperties(acceptance, this, "signer");
        if (acceptance.getSigner() != null) {
            this.signer = acceptance.getSigner().getId();
        }
    }

    public AcceptanceDocument toAcceptanceDocument() {
        AcceptanceDocument acceptance = new AcceptanceDocument();
        BeanUtils.copyProperties(this, acceptance, "signer");
        acceptance.setSigner(UserDto.builder().id(this.signer).build());
        return acceptance;
    }
}
