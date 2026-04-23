package es.upm.api.infrastructure.resources;

import es.upm.api.domain.model.EngagementLetter;
import es.upm.api.domain.model.LegalProcedure;
import es.upm.api.domain.model.PaymentMethod;
import es.upm.api.domain.services.EngagementLetterService;
import es.upm.miw.exception.BadRequestException;
import es.upm.miw.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class PublicEngagementLetterResourceIT {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EngagementLetterService engagementLetterService;

    @Test
    void testReadByToken() throws Exception {
        UUID engagementLetterId = UUID.randomUUID();
        BDDMockito.given(this.engagementLetterService.readPublicByToken(eq("public-token-123")))
                .willReturn(EngagementLetter.builder()
                        .id(engagementLetterId)
                        .creationDate(LocalDate.of(2026, 4, 8))
                        .discount(10)
                        .closingDate(LocalDate.of(2026, 4, 30))
                        .legalProcedures(List.of(LegalProcedure.builder()
                                .title("procedimiento")
                                .budget(BigDecimal.TEN)
                                .legalTasks(List.of("task"))
                                .build()))
                        .paymentMethods(List.of(PaymentMethod.builder().description("Todo").percentage("100%").build()))
                        .build());

        this.mockMvc.perform(get(PublicEngagementLetterResource.PUBLIC_ENGAGEMENT_LETTERS + PublicEngagementLetterResource.ACCESS)
                        .param("token", "public-token-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(engagementLetterId.toString()))
                .andExpect(jsonPath("$.discount").value(10))
                .andExpect(jsonPath("$.legalProcedures[0].title").value("procedimiento"))
                .andExpect(jsonPath("$.paymentMethods[0].description").value("Todo"));
    }

    @Test
    void testReadByTokenWhenTokenDoesNotExist() throws Exception {
        BDDMockito.given(this.engagementLetterService.readPublicByToken(eq("missing-token")))
                .willThrow(new NotFoundException("The PublicAccessToken doesn't exist: missing-token"));

        this.mockMvc.perform(get(PublicEngagementLetterResource.PUBLIC_ENGAGEMENT_LETTERS + PublicEngagementLetterResource.ACCESS)
                        .param("token", "missing-token"))
                .andExpect(status().isNotFound());
    }

}
