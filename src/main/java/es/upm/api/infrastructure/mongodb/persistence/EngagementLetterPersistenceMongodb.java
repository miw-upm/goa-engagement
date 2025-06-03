package es.upm.api.infrastructure.mongodb.persistence;

import es.upm.api.domain.exceptions.NotFoundException;
import es.upm.api.domain.model.EngagementLetter;
import es.upm.api.domain.model.UserDto;
import es.upm.api.domain.persistence.EngagementLetterPersistence;
import es.upm.api.infrastructure.mongodb.entities.AcceptanceDocumentEntity;
import es.upm.api.infrastructure.mongodb.entities.EngagementLetterEntity;
import es.upm.api.infrastructure.mongodb.entities.LegalProcedureEntity;
import es.upm.api.infrastructure.mongodb.entities.PaymentMethodEntity;
import es.upm.api.infrastructure.mongodb.repositories.EngagementLetterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class EngagementLetterPersistenceMongodb implements EngagementLetterPersistence {

    private final EngagementLetterRepository engagementLetterRepository;

    @Autowired
    public EngagementLetterPersistenceMongodb(EngagementLetterRepository engagementLetterRepository) {
        this.engagementLetterRepository = engagementLetterRepository;
    }

    @Override
    public void create(EngagementLetter engagementLetter) {
        EngagementLetterEntity engagementLetterEntity = this.convertToEngagementLetterEntity(engagementLetter);
        this.engagementLetterRepository.save(engagementLetterEntity);
    }

    private EngagementLetterEntity convertToEngagementLetterEntity(EngagementLetter engagementLetter) {
        EngagementLetterEntity engagementLetterEntity = new EngagementLetterEntity(engagementLetter);
        engagementLetterEntity.setOwnerId(engagementLetter.getOwner().getId());
        Optional.ofNullable(engagementLetter.getAttachments())
                .ifPresent(attachments -> engagementLetterEntity.setAttachmentIds(
                        attachments.stream()
                                .map(UserDto::getId)
                                .toList()
                ));
        engagementLetterEntity.setLegalProcedureEntities(
                engagementLetter.getLegalProcedures().stream()
                        .map(LegalProcedureEntity::new)
                        .toList()
        );
        engagementLetterEntity.setPaymentMethodEntities(
                engagementLetter.getPaymentMethods().stream()
                        .map(PaymentMethodEntity::new)
                        .toList()
        );
        Optional.ofNullable(engagementLetter.getAcceptanceDocuments())
                .ifPresent(documents -> engagementLetterEntity.setAcceptanceDocumentEntities(
                        documents.stream()
                                .map(AcceptanceDocumentEntity::new)
                                .toList()
                ));
        return engagementLetterEntity;
    }

    @Override
    public void delete(UUID id) {
        this.engagementLetterRepository.deleteById(id);
    }

    @Override
    public void update(UUID id, EngagementLetter engagementLetter) {
        EngagementLetter engagementLetterBd = this.readById(id);
        engagementLetter.setId(id);
        engagementLetter.setCreationDate(engagementLetterBd.getCreationDate());
        this.engagementLetterRepository.save(this.convertToEngagementLetterEntity(engagementLetter));
    }

    @Override
    public EngagementLetter readById(UUID id) {
        return this.engagementLetterRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("The EngagementLetter ID doesn't exist: " + id))
                .toEngagementLetter();
    }

}
