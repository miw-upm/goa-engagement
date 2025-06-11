package es.upm.api.domain.services;

import es.upm.api.domain.exceptions.NotFoundException;
import es.upm.api.domain.model.EngagementLetter;
import es.upm.api.domain.model.LegalProcedure;
import es.upm.api.domain.model.PaymentMethod;
import es.upm.api.domain.model.UserDto;
import es.upm.api.infrastructure.webclients.UserWebClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
@ActiveProfiles("test")
class EngagementLetterServiceIT {

    @Autowired
    private EngagementLetterService engagementLetterService;

    @MockitoBean
    private UserWebClient userWebClient;
    private EngagementLetter engagementLetter;


    @BeforeEach
    void setUpEngagementLetter() {
        this.engagementLetter = EngagementLetter.builder()
                .discount(15)
                .owner(UserDto.builder().id(UUID.randomUUID()).mobile("123456789").firstName("John").build())
                .legalProcedures(List.of(LegalProcedure.builder()
                        .title("procedimiento").budget(BigDecimal.TEN).legalTasks(List.of("tarea")).build()))
                .paymentMethods(List.of(PaymentMethod.builder().description("Todo").percentage(100).build()))
                .build();

        BDDMockito.given(this.userWebClient.readUserByMobile(any(String.class)))
                .willAnswer(invocation ->
                        UserDto.builder().id(this.engagementLetter.getOwner().getId()).mobile(invocation.getArgument(0)).firstName("mock").build());
        BDDMockito.given(this.userWebClient.readUserById(any(UUID.class)))
                .willAnswer(invocation ->
                        UserDto.builder().id(invocation.getArgument(0)).mobile("123456789").firstName("mock").build());
        this.engagementLetterService.create(this.engagementLetter);
    }

    @Test
    void testReadSuccess() {
        assertThat(engagementLetterService.readById(UUID.fromString("aaaaaaa0-bbbb-cccc-dddd-eeeeffff0001")))
                .isNotNull()
                .satisfies(retrieveEngagement -> {
                    assertThat(retrieveEngagement.getOwner().getFirstName()).isEqualTo("mock");
                    assertThat(retrieveEngagement.getOwner().getMobile()).isEqualTo("123456789");
                    assertThat(retrieveEngagement.getDiscount()).isEqualTo(20);
                });
    }

    @Test
    void testCreateSuccess() {
        assertThat(this.engagementLetter.getId()).isNotNull();
        EngagementLetter engagementLetterDb = this.engagementLetterService.readById(engagementLetter.getId());
        assertThat(engagementLetterDb)
                .isNotNull()
                .satisfies(engagement -> {
                    assertThat(engagement.getDiscount()).isEqualTo(15);
                    assertThat(engagement.getPaymentMethods()).hasSize(1);
                    assertThat(engagement.getPaymentMethods().getFirst().getDescription()).isEqualTo("Todo");
                    assertThat(engagement.getAttachments()).isNull();
                    assertThat(engagement.getLegalProcedures()).hasSize(1);
                    assertThat(engagement.getLegalProcedures().getFirst().getTitle()).isEqualTo("procedimiento");
                });
    }

    @Test
    void testUpdateSuccess() {
        UUID originalId = this.engagementLetter.getId();
        EngagementLetter updatedEngagementLetter = EngagementLetter.builder()
                .id(originalId)
                .discount(30)
                .owner(engagementLetter.getOwner())
                .legalProcedures(engagementLetter.getLegalProcedures())
                .paymentMethods(List.of(PaymentMethod.builder().description("Actualizado").percentage(100).build()))
                .build();
        this.engagementLetterService.update(originalId, updatedEngagementLetter);

        EngagementLetter retrieved = this.engagementLetterService.readById(originalId);
        assertThat(retrieved)
                .isNotNull()
                .satisfies(letter -> {
                    assertThat(letter.getDiscount()).isEqualTo(30);
                    assertThat(letter.getPaymentMethods()).hasSize(1);
                    assertThat(letter.getPaymentMethods().getFirst().getDescription()).isEqualTo("Actualizado");
                });
    }

    @Test
    void testDeleteSuccess() {
        UUID engagementLetterId = this.engagementLetter.getId();
        this.engagementLetterService.delete(engagementLetterId);
        assertThatThrownBy(() -> this.engagementLetterService.readById(engagementLetterId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(engagementLetterId.toString());
    }

}
