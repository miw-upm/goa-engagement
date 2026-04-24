package es.upm.api.adapter.out.legal.mongo.engagementletter;

import es.upm.api.domain.model.EngagementLetter;
import es.upm.api.domain.model.criteria.EngagementLetterFindCriteria;
import es.upm.api.domain.ports.out.legal.EngagementLetterGateway;
import es.upm.miw.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.UUID;
import java.util.stream.Stream;

@Repository
@RequiredArgsConstructor
public class EngagementLetterAdapter implements EngagementLetterGateway {

    private final EngagementLetterRepository engagementLetterRepository;

    @Override
    public void create(EngagementLetter engagementLetter) {
        this.engagementLetterRepository.save(new EngagementLetterEntity(engagementLetter));
    }

    @Override
    public void delete(UUID id) {
        this.engagementLetterRepository.deleteById(id);
    }

    @Override
    public void update(UUID id, EngagementLetter engagementLetter) {
        if (id != engagementLetter.getId() || !this.engagementLetterRepository.existsById(id)) {
            throw new NotFoundException("For update The EngagementLetter must exist: " + id);
        }
        this.engagementLetterRepository.save(new EngagementLetterEntity(engagementLetter));
    }

    @Override
    public Stream<EngagementLetter> find(EngagementLetterFindCriteria criteria) {
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

        return letters.map(EngagementLetterEntity::toDomain);
    }

    @Override
    public EngagementLetter readById(UUID id) {
        return this.engagementLetterRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("The EngagementLetter ID doesn't exist: " + id))
                .toDomain();
    }

}
