package es.upm.api.domain.services;

import es.upm.api.domain.exceptions.NotFoundException;
import es.upm.api.domain.model.ProcedimientoLegal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class ProcedimientoLegalServiceIT {
    @Autowired
    private ProcedimientoLegalService procedimientoLegalService;

    private static final UUID EXISTING_ID = UUID.fromString("aaaaaaa0-bbbb-cccc-dddd-eeeeffff0000");

    @Test
    void shouldFindById() {
        assertThat(procedimientoLegalService.readById(EXISTING_ID))
                .isNotNull()
                .extracting(ProcedimientoLegal::getTitulo)
                .isEqualTo("Procedimiento de herencia.");
    }

    @Test
    void shouldFindByKeyword() {
        assertThat(procedimientoLegalService.findNullSafe("herencia").toList())
                .isNotEmpty()
                .extracting(ProcedimientoLegal::getTitulo)
                .anySatisfy(titulo -> assertThat(titulo.toLowerCase()).contains("herencia"));
    }

    @Test
    void shouldUpdateTitulo() {
        ProcedimientoLegal nuevo = ProcedimientoLegal.builder().titulo("Título original").build();
        this.procedimientoLegalService.create(nuevo);
        UUID id = nuevo.getId();
        nuevo.setTitulo("Título actualizado");
        this.procedimientoLegalService.update(id, nuevo);
        ProcedimientoLegal actualizado = this.procedimientoLegalService.readById(id);
        assertThat(actualizado.getTitulo())
                .isEqualTo("Título actualizado");
        this.procedimientoLegalService.delete(id);
    }

    @Test
    void shouldDelete() {
        ProcedimientoLegal nuevo = ProcedimientoLegal.builder().titulo("Temporal para borrado").build();
        this.procedimientoLegalService.create(nuevo);
        UUID id = nuevo.getId();
        assertThat(procedimientoLegalService.readById(id)).isNotNull();
        this.procedimientoLegalService.delete(id);
        assertThatThrownBy(() -> procedimientoLegalService.readById(id))
                .isInstanceOf(NotFoundException.class);
    }
}
