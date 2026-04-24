package es.upm.api.domain.services;

import es.upm.api.domain.model.LegalTask;
import es.upm.api.domain.ports.out.legal.LegalTaskGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class LegalTaskService {

    private final LegalTaskGateway legalTaskPersistence;
    private final LegalProcedureTemplateService legalProcedureTemplateService;

    public void create(LegalTask legalTask) {
        legalTask.setId(UUID.randomUUID());
        this.legalTaskPersistence.create(legalTask);
    }

    public void deleteById(UUID id) {
        this.legalProcedureTemplateService.findAll()
                .forEach(legalProcedureTemplate -> {
                    List<LegalTask> legalTasks = legalProcedureTemplate.getLegalTasks();
                    List<LegalTask> updatedTasks = legalTasks.stream()
                            .filter(legalTask -> !id.equals(legalTask.getId()))
                            .toList();
                    if (updatedTasks.size() != legalTasks.size()) {
                        legalProcedureTemplate.setLegalTasks(updatedTasks);
                        this.legalProcedureTemplateService.update(legalProcedureTemplate.getId(), legalProcedureTemplate);
                    }
                });
        this.legalTaskPersistence.deleteById(id);
    }

    public void update(UUID id, LegalTask legalTask) {
        this.legalTaskPersistence.update(id, legalTask);
    }

    public Stream<LegalTask> find(String title) {
        return this.legalTaskPersistence.find(title);
    }

    public LegalTask read(UUID id) {
        return this.legalTaskPersistence.read(id);
    }
}
