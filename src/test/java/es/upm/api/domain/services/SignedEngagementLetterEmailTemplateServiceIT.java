package es.upm.api.domain.services;

import static org.assertj.core.api.Assertions.assertThat;

import es.upm.miw.mail.Email;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class SignedEngagementLetterEmailTemplateServiceIT {

    @Autowired
    private SignedEngagementLetterEmailTemplateService signedEngagementLetterEmailTemplateService;

    @Test
    void testBuildHtmlEmailSuccess() {
        Email email = signedEngagementLetterEmailTemplateService.buildHtmlEmail(
                "client@example.com",
                "John"
        );

        assertThat(email)
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.getTo()).isEqualTo("client@example.com");
                    assertThat(result.getSubject()).isEqualTo("Firmado Hoja de Encargo en Ocaña Abogados");
                    assertThat(result.getBody()).isNotBlank();
                    assertThat(result.getBody()).contains("John");
                    assertThat(result.getBody()).doesNotContain("FIRST_NAME");
                });
    }
}