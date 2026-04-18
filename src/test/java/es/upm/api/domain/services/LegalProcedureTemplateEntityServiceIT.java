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
class LegalProcedureTemplateEntityServiceIT {
    private static final UUID EXISTING_ID = UUID.fromString("aaaaaaa0-bbbb-cccc-dddd-eeeeffff0000");

    @Autowired
    private LegalProcedureTemplateService legalProcedureTemplateService;

    @Test
    void shouldFindById() {
        assertThat(legalProcedureTemplateService.readById(EXISTING_ID))
                .isNotNull()
                .extracting(LegalProcedureTemplate::getTitle)
                .isEqualTo("Procedimiento de herencia");
    }

    @Test
    void shouldSearchByTitle() {
        List<LegalProcedureTemplate> results = legalProcedureTemplateService
                .searchByTitleAndTaskTitleNullSafe("herencia", null)
                .toList();

        assertThat(results)
                .isNotEmpty()
                .anyMatch(p -> p.getTitle().toLowerCase().contains("herencia"));
    }

    @Test
    void shouldSearchByTaskTitle() {
        List<LegalProcedureTemplate> results = legalProcedureTemplateService
                .searchByTitleAndTaskTitleNullSafe(null, "escritura")
                .toList();

        assertThat(results)
                .isNotEmpty()
                .anyMatch(p -> p.getLegalTasks().stream()
                        .anyMatch(t -> t.getTitle().toLowerCase().contains("escritura")));
    }

    @Test
    void shouldSearchByTitleAndTaskTitle() {
        List<LegalProcedureTemplate> results = legalProcedureTemplateService
                .searchByTitleAndTaskTitleNullSafe("herencia", "escritura")
                .toList();

        assertThat(results)
                .isNotEmpty()
                .anyMatch(p -> p.getTitle().toLowerCase().contains("herencia"));
    }

    @Test
    void shouldReturnAllWhenBothNull() {
        List<LegalProcedureTemplate> results = legalProcedureTemplateService
                .searchByTitleAndTaskTitleNullSafe(null, null)
                .toList();

        assertThat(results).hasSizeGreaterThanOrEqualTo(1);
    }

    @Test
    void shouldReturnEmptyWhenNoMatch() {
        List<LegalProcedureTemplate> results = legalProcedureTemplateService
                .searchByTitleAndTaskTitleNullSafe("xyznoexiste999", null)
                .toList();

        assertThat(results).isEmpty();
    }

    @Test
    void shouldSearchIgnoreCase() {
        List<LegalProcedureTemplate> upper = legalProcedureTemplateService
                .searchByTitleAndTaskTitleNullSafe("HERENCIA", null).toList();
        List<LegalProcedureTemplate> lower = legalProcedureTemplateService
                .searchByTitleAndTaskTitleNullSafe("herencia", null).toList();

        assertThat(upper)
                .isNotEmpty()
                .hasSameSizeAs(lower);
    }

    @Test
    void shouldThrowNotFoundForInvalidId() {
        assertThatThrownBy(() -> legalProcedureTemplateService.readById(UUID.randomUUID()))
                .isInstanceOf(NotFoundException.class);
    }
}