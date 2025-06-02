package es.upm.api.infrastructure.mongodb.entities;

import es.upm.api.domain.model.EngagementLetter;
import es.upm.api.domain.model.UserDto;
import lombok.*;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document
public class EngagementLetterEntity {
    @Id
    private UUID id;
    private Integer discount;
    private LocalDate creationDate;
    private LocalDate closingDate;
    private UUID ownerId;
    @Singular
    private List<UUID> attachmentIds;
    @Singular
    private List<LegalProcedureEntity> legalProcedureEntities;
    @Singular
    private List<PaymentMethodEntity> paymentMethodEntities;
    @Singular
    private List<AcceptanceDocumentEntity> acceptanceDocumentEntities;

    public EngagementLetterEntity(EngagementLetter engagementLetter) {
        BeanUtils.copyProperties(engagementLetter, this);
        this.ownerId = engagementLetter.getOwner().getId();
    }

    public EngagementLetter toEngagementLetter() {
        EngagementLetter engagementLetter = new EngagementLetter();
        BeanUtils.copyProperties(this, engagementLetter);
        engagementLetter.setOwner(UserDto.builder().id(this.getOwnerId()).build());

        if (this.attachmentIds != null) {
            engagementLetter.setAttachments(this.attachmentIds.stream()
                    .map(attachmentId -> UserDto.builder().id(attachmentId).build())
                    .toList());
        }
        if (this.acceptanceDocumentEntities != null) {
            engagementLetter.setAcceptanceDocuments(this.acceptanceDocumentEntities.stream()
                    .map(AcceptanceDocumentEntity::toAcceptanceDocument)
                    .toList());
        }
        engagementLetter.setPaymentMethods(this.paymentMethodEntities.stream()
                .map(PaymentMethodEntity::toPaymentMethod)
                .toList());
        engagementLetter.setLegalProcedures(this.legalProcedureEntities.stream()
                .map(LegalProcedureEntity::toLegalProcedure)
                .toList());
        return engagementLetter;
    }
}
