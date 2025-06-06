package es.upm.api.infrastructure.mongodb.persistence;

import es.upm.api.domain.exceptions.NotFoundException;
import es.upm.api.domain.model.EngagementLetter;
import es.upm.api.domain.model.EngagementLetterFindCriteria;
import es.upm.api.domain.model.UserDto;
import es.upm.api.domain.persistence.EngagementLetterPersistence;
import es.upm.api.infrastructure.mongodb.entities.AcceptanceDocumentEntity;
import es.upm.api.infrastructure.mongodb.entities.EngagementLetterEntity;
import es.upm.api.infrastructure.mongodb.entities.LegalProcedureEntity;
import es.upm.api.infrastructure.mongodb.entities.PaymentMethodEntity;
import es.upm.api.infrastructure.mongodb.repositories.EngagementLetterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

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
        System.out.println(">>>> ENTITY: " + engagementLetterEntity);
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
    public Stream<EngagementLetter> findNullSafe(EngagementLetterFindCriteria criteria) {
        List<EngagementLetterEntity> result;
        if (Boolean.TRUE.equals(criteria.getOpened())) {
            result = this.engagementLetterRepository.findByOpened();
        } else {
            result = this.engagementLetterRepository.findByClosed();
        }
        if (criteria.getLegalProcedureTitle() != null) {
            return result.stream()
                    .filter(engagement -> engagement.getLegalProcedureEntities().stream()
                            .anyMatch(procedure -> procedure.getTitle().toLowerCase()
                                    .contains(criteria.getLegalProcedureTitle().toLowerCase())))
                    .map(EngagementLetterEntity::toEngagementLetter);
        } else {
            return result.stream()
                    .map(EngagementLetterEntity::toEngagementLetter);
        }

    }

    @Override
    public EngagementLetter readById(UUID id) {
        return this.engagementLetterRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("The EngagementLetter ID doesn't exist: " + id))
                .toEngagementLetter();
    }

}
