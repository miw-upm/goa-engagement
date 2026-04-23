package es.upm.api.domain.services;

import es.upm.api.domain.model.snapshos.UserSnapshot;
import es.upm.api.domain.webclients.UserWebClient;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
@ActiveProfiles("test")
class EngagementLetterPdfCheck {

    @Autowired
    private EngagementLetterService engagementLetterService;

    @MockitoBean
    private UserWebClient userWebClient;

    @Test
    void testGeneratePresupuestoPdfCheck() throws Exception {
        BDDMockito.given(this.userWebClient.readUserById(any(UUID.class)))
                .willReturn(UserSnapshot.builder()
                        .id(UUID.randomUUID())
                        .firstName("María")
                        .familyName("García López")
                        .mobile("612345678")
                        .documentType("DNI")
                        .identity("43234543V")
                        .build());

        byte[] pdf = this.engagementLetterService.generatePdf(
                UUID.fromString("aaaaaaa0-bbbb-cccc-dddd-eeeeffff0000"));

        Path output = Path.of("target", "presupuesto-check.pdf");
        Files.write(output, pdf);
        System.out.println("PDF generado en: " + output.toAbsolutePath());
    }

    @Test
    void testGenerateHojaPdfCheck() throws Exception {
        BDDMockito.given(this.userWebClient.readUserById(any(UUID.class)))
                .willReturn(UserSnapshot.builder()
                        .id(UUID.randomUUID())
                        .firstName("María")
                        .familyName("García López")
                        .mobile("612345678")
                        .documentType("DNI")
                        .identity("43234543V")
                        .build());

        byte[] pdf = this.engagementLetterService.generatePdf(
                UUID.fromString("aaaaaaa0-bbbb-cccc-dddd-eeeeffff0002"));

        Path output = Path.of("target", "hoja-check.pdf");
        Files.write(output, pdf);
        System.out.println("PDF generado en: " + output.toAbsolutePath());
    }
}
