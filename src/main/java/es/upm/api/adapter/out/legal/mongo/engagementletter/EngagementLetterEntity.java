package es.upm.api.adapter.out.legal.mongo.engagementletter;

import es.upm.api.domain.model.EngagementLetter;
import es.upm.api.domain.model.external.UserSnapshot;
import lombok.*;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document
public class EngagementLetterEntity {
    @Id
    private UUID id;
    private Boolean budgetOnly;
    private LocalDate lastUpdatedDate;
    private Integer discount;
    private LocalDate closingDate;
    private UUID ownerId;
    @Singular
    private List<UUID> attachmentIds;
    @Singular
    private List<LegalProcedureEntity> legalProcedureEntities;
    @Singular
    private List<PaymentMethodEntity> paymentMethodEntities;
    private String legalClause;
    @Singular
    private List<AcceptanceEngagementEntity> acceptanceEngagementEntities;

    public EngagementLetterEntity(EngagementLetter engagementLetter) {
        BeanUtils.copyProperties(engagementLetter, this);
        this.ownerId = engagementLetter.getOwner().getId();
        Optional.ofNullable(engagementLetter.getAttachments())
                .ifPresent(attachments -> this.setAttachmentIds(
                        attachments.stream()
                                .map(UserSnapshot::getId)
                                .toList()
                ));
        this.setLegalProcedureEntities(
                engagementLetter.getLegalProcedures().stream()
                        .map(LegalProcedureEntity::new)
                        .toList()
        );
        this.setPaymentMethodEntities(
                engagementLetter.getPaymentMethods().stream()
                        .map(PaymentMethodEntity::new)
                        .toList()
        );

        Optional.ofNullable(engagementLetter.getAcceptanceEngagements())
                .ifPresent(documents -> this.setAcceptanceEngagementEntities(
                        documents.stream()
                                .map(AcceptanceEngagementEntity::new)
                                .toList()
                ));
    }

    public EngagementLetter toDomain() {
        EngagementLetter engagementLetter = new EngagementLetter();
        BeanUtils.copyProperties(this, engagementLetter);
        engagementLetter.setOwner(UserSnapshot.builder().id(this.getOwnerId()).build());

        if (this.attachmentIds != null) {
            engagementLetter.setAttachments(this.attachmentIds.stream()
                    .map(attachmentId -> UserSnapshot.builder().id(attachmentId).build())
                    .toList());
        }
        if (this.acceptanceEngagementEntities != null) {
            engagementLetter.setAcceptanceEngagements(this.acceptanceEngagementEntities.stream()
                    .map(AcceptanceEngagementEntity::toDomain)
                    .toList());
        }
        engagementLetter.setPaymentMethods(this.paymentMethodEntities.stream()
                .map(PaymentMethodEntity::toDomain)
                .toList());
        engagementLetter.setLegalProcedures(this.legalProcedureEntities.stream()
                .map(LegalProcedureEntity::toDomain)
                .toList());
        return engagementLetter;
    }
}
