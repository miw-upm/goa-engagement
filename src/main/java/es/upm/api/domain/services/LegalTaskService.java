package es.upm.api.domain.services;

import es.upm.api.domain.model.LegalTask;
import es.upm.api.domain.persistence.LegalTaskPersistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class LegalTaskService {

    private final LegalTaskPersistence legalTaskPersistence;
    private final LegalProcedureTemplateService legalProcedureTemplateService;

    @Autowired
    public LegalTaskService(LegalTaskPersistence legalTaskPersistence, LegalProcedureTemplateService legalProcedureTemplateService) {
        this.legalTaskPersistence = legalTaskPersistence;
        this.legalProcedureTemplateService = legalProcedureTemplateService;
    }

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
        return this.legalTaskPersistence.findNullSafe(title);
    }

    public Stream<LegalTask> findAll() {
        return this.legalTaskPersistence.findAll();
    }

    public LegalTask read(UUID id) {
        return this.legalTaskPersistence.read(id);
    }
}
