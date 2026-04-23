package es.upm.api.adapter.out.legal.mongo.engagementletter;

import es.upm.api.domain.model.EngagementLetter;
import es.upm.api.domain.model.criteria.EngagementLetterCriteria;
import es.upm.api.domain.model.snapshots.UserSnapshot;
import es.upm.api.domain.ports.out.legal.EngagementLetterGateway;
import es.upm.miw.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Repository
@RequiredArgsConstructor
public class EngagementLetterPersistenceMongodb implements EngagementLetterGateway {

    private final EngagementLetterRepository engagementLetterRepository;

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
                                .map(UserSnapshot::getId)
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
        engagementLetterEntity.setLegalClause(engagementLetter.getLegalClause());
        Optional.ofNullable(engagementLetter.getAcceptanceEngagements())
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
        this.readById(id);
        engagementLetter.setId(id);
        this.engagementLetterRepository.save(this.convertToEngagementLetterEntity(engagementLetter));
    }

    @Override
    public Stream<EngagementLetter> searchNullSafe(EngagementLetterCriteria criteria) {
        Stream<EngagementLetterEntity> letters = this.engagementLetterRepository
                .findAll(Sort.by(Sort.Direction.DESC, "creationDate")).stream();

        if (criteria.getOpened() != null) {
            letters = letters.filter(letter -> criteria.getOpened() == (letter.getClosingDate() == null));
        }

        if (criteria.getBudgetOnly() != null) {
            letters = letters.filter(letter -> criteria.getBudgetOnly().equals(letter.getBudgetOnly()));
        }

        if (StringUtils.hasText(criteria.getLegalProcedureTitle())) {
            String titleLower = criteria.getLegalProcedureTitle().toLowerCase();
            letters = letters.filter(letter -> letter.getLegalProcedureEntities() != null &&
                    letter.getLegalProcedureEntities().stream()
                            .anyMatch(proc -> proc.getTitle() != null &&
                                    proc.getTitle().toLowerCase().contains(titleLower)));
        }

        if (StringUtils.hasText(criteria.getTaskTitle())) {
            String taskLower = criteria.getTaskTitle().toLowerCase();
            letters = letters.filter(letter -> letter.getLegalProcedureEntities() != null &&
                    letter.getLegalProcedureEntities().stream()
                            .anyMatch(proc -> proc.getLegalTasks() != null &&
                                    proc.getLegalTasks().stream()
                                            .anyMatch(task -> task != null &&
                                                    task.toLowerCase().contains(taskLower))));
        }

        return letters.map(EngagementLetterEntity::toEngagementLetter);
    }

    @Override
    public EngagementLetter readById(UUID id) {
        return this.engagementLetterRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("The EngagementLetter ID doesn't exist: " + id))
                .toEngagementLetter();
    }

}
