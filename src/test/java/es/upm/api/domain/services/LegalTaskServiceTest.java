package es.upm.api.domain.services;

import es.upm.api.domain.model.LegalProcedureTemplate;
import es.upm.api.domain.model.LegalTask;
import es.upm.api.domain.ports.out.legal.LegalTaskGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LegalTaskServiceTest {
    @Mock
    private LegalTaskGateway legalTaskPersistence;
    @Mock
    private LegalProcedureTemplateService legalProcedureTemplateService;
    @InjectMocks
    private LegalTaskService legalTaskService;

    @ParameterizedTest
    @NullSource
    @ValueSource(booleans = false)
    void shouldKeepExistingSearchWhenUnassignedIsNotTrue(Boolean unassigned) {
        LegalTask task = LegalTask.builder().id(UUID.randomUUID()).title("Task").build();
        when(this.legalTaskPersistence.find(null)).thenReturn(Stream.of(task));

        assertThat(this.legalTaskService.find(null, unassigned)).containsExactly(task);
        verifyNoInteractions(this.legalProcedureTemplateService);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "Task")
    void shouldExcludeTasksBelongingToAnyTemplate(String title) {
        LegalTask assigned = LegalTask.builder().id(UUID.randomUUID()).title("Task assigned").build();
        LegalTask unassigned = LegalTask.builder().id(UUID.randomUUID()).title("Task free").build();
        LegalTask assignedSnapshot = LegalTask.builder().id(assigned.getId()).title("Other title").build();
        when(this.legalTaskPersistence.find(title)).thenReturn(Stream.of(assigned, unassigned));
        when(this.legalProcedureTemplateService.findAll()).thenReturn(Stream.of(
                LegalProcedureTemplate.builder().legalTasks(List.of()).build(),
                LegalProcedureTemplate.builder().legalTasks(List.of(assignedSnapshot)).build()));

        assertThat(this.legalTaskService.find(title, true)).containsExactly(unassigned);
    }

    @Test
    void shouldReturnAllMatchingTasksWhenThereAreNoTemplates() {
        LegalTask task = LegalTask.builder().id(UUID.randomUUID()).title("Task").build();
        when(this.legalTaskPersistence.find(null)).thenReturn(Stream.of(task));
        when(this.legalProcedureTemplateService.findAll()).thenReturn(Stream.empty());

        assertThat(this.legalTaskService.find(null, true)).containsExactly(task);
    }
}
