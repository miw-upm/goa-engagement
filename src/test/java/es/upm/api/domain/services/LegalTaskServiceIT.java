package es.upm.api.domain.services;

import es.upm.api.domain.exceptions.NotFoundException;
import es.upm.api.domain.model.LegalProcedureTemplate;
import es.upm.api.domain.model.LegalTask;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class LegalTaskServiceIT {

    @Autowired
    private LegalTaskService legalTaskService;

    @Autowired
    private LegalProcedureTemplateService legalProcedureTemplateService;

    @Test
    void shouldDeleteTaskAndRemoveItFromProcedureTemplate() {
        String suffix = UUID.randomUUID().toString();

        LegalTask taskToDelete = LegalTask.builder().title("Task delete " + suffix).build();
        this.legalTaskService.create(taskToDelete);

        LegalTask taskToKeep = LegalTask.builder().title("Task keep " + suffix).build();
        this.legalTaskService.create(taskToKeep);

        LegalProcedureTemplate template = LegalProcedureTemplate.builder()
                .title("Template " + suffix)
                .legalTasks(List.of(taskToDelete.ofTitle(), taskToKeep.ofTitle()))
                .build();
        this.legalProcedureTemplateService.create(template);

        UUID taskToDeleteId = taskToDelete.getId();
        UUID taskToKeepId = taskToKeep.getId();
        UUID templateId = template.getId();

        this.legalTaskService.deleteById(taskToDeleteId);

        assertThatThrownBy(() -> this.legalTaskService.read(taskToDeleteId))
                .isInstanceOf(NotFoundException.class);

        LegalProcedureTemplate updatedTemplate = this.legalProcedureTemplateService.readById(templateId);
        assertThat(updatedTemplate.getLegalTasks())
                .extracting(LegalTask::getId)
                .contains(taskToKeepId)
                .doesNotContain(taskToDeleteId);

        this.legalProcedureTemplateService.delete(templateId);
        this.legalTaskService.deleteById(taskToKeepId);
    }
}
