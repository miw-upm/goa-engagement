package es.upm.api.domain.services;

import es.upm.api.adapter.out.user.feign.GoaUserClient;
import es.upm.api.domain.model.external.UserSnapshot;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static es.upm.api.configurations.DatabaseSeederDev.UUIDS;
import static org.mockito.ArgumentMatchers.any;

@Log4j2
@SpringBootTest
@ActiveProfiles("test")
class EngagementLetterPdfCheck {

    @Autowired
    private EngagementLetterService engagementLetterService;

    @MockitoBean
    private GoaUserClient userFinderClient;

    @Test
    void testGenerateBudgePdfCheck() throws Exception {
        BDDMockito.given(this.userFinderClient.readUserById(any(UUID.class)))
                .willReturn(UserSnapshot.builder()
                        .id(UUID.randomUUID())
                        .firstName("María")
                        .familyName("García López")
                        .mobile("612345678")
                        .documentType("DNI")
                        .identity("43234543V")
                        .build());
        byte[] pdf = this.engagementLetterService.generatePdf(UUIDS[0]);
        Path output = Path.of("target", "presupuesto-check.pdf");
        Files.write(output, pdf);
        log.info("PDF generado en: {}", output.toAbsolutePath());
    }

    @Test
    void testGenerateLetterPdfCheck() throws Exception {
        BDDMockito.given(this.userFinderClient.readUserById(any(UUID.class)))
                .willReturn(UserSnapshot.builder()
                        .id(UUID.randomUUID())
                        .firstName("María")
                        .familyName("García López")
                        .mobile("612345678")
                        .documentType("DNI")
                        .identity("43234543V")
                        .build());
        byte[] pdf = this.engagementLetterService.generatePdf(UUIDS[0]);
        Path output = Path.of("target", "hoja-check.pdf");
        Files.write(output, pdf);
        log.info("PDF generado en: {}", output.toAbsolutePath());
    }
}
