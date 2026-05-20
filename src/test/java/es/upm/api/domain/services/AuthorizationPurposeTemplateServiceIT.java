package es.upm.api.domain.services;

import es.upm.api.domain.model.AuthorizationPurposeTemplate;
import es.upm.miw.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static es.upm.api.configurations.DatabaseSeederDev.ID_0;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class AuthorizationPurposeTemplateServiceIT {

    @Autowired
    private AuthorizationPurposeTemplateService authorizationPurposeTemplateService;

    @Test
    void shouldFindById() {
        assertThat(this.authorizationPurposeTemplateService.read(ID_0))
                .isNotNull()
                .extracting(AuthorizationPurposeTemplate::getPurpose)
                .isEqualTo("Gestiones bancarias y de seguros");
    }

    @Test
    void shouldThrowNotFoundForInvalidId() {
        assertThatThrownBy(() -> this.authorizationPurposeTemplateService.read(UUID.randomUUID()))
                .isInstanceOf(NotFoundException.class);
    }
}
