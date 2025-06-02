package es.upm.api.domain.services;

import es.upm.api.domain.exceptions.NotFoundException;
import es.upm.api.domain.model.LegalProcedureTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

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
    void shouldFindByKeyword() {
        assertThat(legalProcedureTemplateService.findNullSafe("herencia").toList())
                .isNotEmpty()
                .extracting(LegalProcedureTemplate::getTitle)
                .anySatisfy(titulo -> assertThat(titulo.toLowerCase()).contains("herencia"));
    }

    @Test
    void shouldUpdateTitle() {
        LegalProcedureTemplate nuevo = LegalProcedureTemplate.builder().title("Título original").build();
        this.legalProcedureTemplateService.create(nuevo);
        UUID id = nuevo.getId();
        nuevo.setTitle("Título actualizado");
        this.legalProcedureTemplateService.update(id, nuevo);
        LegalProcedureTemplate actualizado = this.legalProcedureTemplateService.readById(id);
        assertThat(actualizado.getTitle())
                .isEqualTo("Título actualizado");
        this.legalProcedureTemplateService.delete(id);
    }

    @Test
    void shouldDelete() {
        LegalProcedureTemplate nuevo = LegalProcedureTemplate.builder().title("Temporal para borrado").build();
        this.legalProcedureTemplateService.create(nuevo);
        UUID id = nuevo.getId();
        assertThat(legalProcedureTemplateService.readById(id)).isNotNull();
        this.legalProcedureTemplateService.delete(id);
        assertThatThrownBy(() -> legalProcedureTemplateService.readById(id))
                .isInstanceOf(NotFoundException.class);
    }
}
