package es.upm.api.domain.services;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TextDictionaryTest {

    @Test
    void shouldReadYamlTemplateKeepingTitlesTextsAndLists() {
        TextDictionary dict = new TextDictionary("templates/engagement-letter-texts.yml");

        assertThat(dict.getTitle("presupuesto")).isEqualTo("PRESUPUESTO DE HONORARIOS");
        assertThat(dict.getText("responsable", Map.of("solicitante", "Ana")))
                .isEqualTo("Por solicitud de Ana, se realiza el siguiente presupuesto:");
        assertThat(dict.getList("banco"))
                .containsExactly(
                        "Cuenta: ES09 1465 0100 96 1707148504",
                        "Entidad: ING",
                        "Titular: Nuria Ocaña Pérez"
                );
        assertThat(dict.getText("condiciones_generales")).contains("\n\n");
    }
}
