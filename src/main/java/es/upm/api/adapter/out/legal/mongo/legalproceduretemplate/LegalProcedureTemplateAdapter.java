package es.upm.api.adapter.out.legal.mongo.legalproceduretemplate;

import es.upm.api.domain.model.LegalProcedureTemplate;
import es.upm.api.domain.model.LegalTask;
import es.upm.api.domain.model.criteria.LegalProcedureTemplateFindCriteria;
import es.upm.api.domain.ports.out.legal.LegalProcedureTemplateGateway;
import es.upm.api.adapter.out.legal.mongo.legaltask.LegalTaskEntity;
import es.upm.api.adapter.out.legal.mongo.legaltask.LegalTaskRepository;
import es.upm.miw.exception.ConflictException;
import es.upm.miw.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Repository
@RequiredArgsConstructor
public class LegalProcedureTemplateAdapter implements LegalProcedureTemplateGateway {

    private final LegalProcedureTemplateRepository procedureRepository;
    private final LegalTaskRepository taskRepository;

    @Override
    public void create(LegalProcedureTemplate procedure) {
        this.assertNotExist(procedure.getTitle());
        LegalProcedureTemplateEntity entity = new LegalProcedureTemplateEntity(procedure);
        entity.setLegalTaskEntities(readTaskAssured(procedure.getLegalTasks()));
        this.procedureRepository.save(entity);
    }

    private List<LegalTaskEntity> readTaskAssured(List<LegalTask> tasks) {
        return tasks.stream()
                .map(legalTask -> this.taskRepository.findByTitle(legalTask.getTitle())
                        .orElseThrow(() -> new NotFoundException("Legal Task not found, title: " + legalTask.getTitle())))
                .toList();
    }

    private void assertNotExist(String title) {
        if (procedureRepository.findByTitle(title).isPresent()) {
            throw new ConflictException("A legal task with a similar title already exists: " + title);
        }
    }

    @Override
    public void deleteById(UUID id) {
        this.procedureRepository.deleteById(id);
    }

    @Override
    public LegalProcedureTemplate read(UUID id) {
        return this.procedureRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("LegalProcedureTemplate id not found: " + id))
                .toLegalProcedureTemplate();

    }

    @Override
    public Stream<LegalProcedureTemplate> find(LegalProcedureTemplateFindCriteria criteria) {
        Stream<LegalProcedureTemplateEntity> templates = StringUtils.hasText(criteria.getTitle())
                ? this.procedureRepository.searchByTitleContainingIgnoreCase(criteria.getTitle(), Sort.by("title")).stream()
                : this.procedureRepository.findAll(Sort.by("title")).stream();

        if (StringUtils.hasText(criteria.getTitle())) {
            String taskTitleLower = criteria.getTitle().toLowerCase();
            templates = templates.filter(template -> template.getLegalTaskEntities() != null &&
                    template.getLegalTaskEntities().stream()
                            .anyMatch(task -> task.getTitle() != null &&
                                    task.getTitle().toLowerCase().contains(taskTitleLower)));
        }

        return templates.map(LegalProcedureTemplateEntity::toLegalProcedureTemplate);
    }

    @Override
    public Stream<LegalProcedureTemplate> findAll() {
        return this.procedureRepository.findAll().stream().map(LegalProcedureTemplateEntity::toLegalProcedureTemplate);
    }

    @Override
    public void update(UUID id, LegalProcedureTemplate procedure) {
        LegalProcedureTemplate procedureDb = this.read(id);
        procedure.setId(id);
        if (!procedureDb.getTitle().equals(procedure.getTitle())) {
            this.assertNotExist(procedure.getTitle());
        }
        LegalProcedureTemplateEntity entity = new LegalProcedureTemplateEntity(procedure);
        entity.setLegalTaskEntities(readTaskAssured(procedure.getLegalTasks()));
        this.procedureRepository.save(entity);
    }
}
