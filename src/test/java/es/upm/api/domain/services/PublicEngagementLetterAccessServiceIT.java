package es.upm.api.domain.services;

import es.upm.api.domain.model.*;
import es.upm.api.domain.persistence.EngagementLetterPersistence;
import es.upm.api.domain.persistence.PublicAccessTokenPersistence;
import es.upm.api.domain.webclients.UserWebClient;
import es.upm.miw.exception.BadRequestException;
import es.upm.miw.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
class PublicEngagementLetterAccessServiceIT {

    @Autowired
    private EngagementLetterService engagementLetterService;

    @MockitoBean
    private EngagementLetterPersistence engagementLetterPersistence;
    @MockitoBean
    private PublicAccessTokenPersistence publicAccessTokenPersistence;
    @MockitoBean
    private UserWebClient userWebClient;

    @Test
    void testReadPublicEngagementLetterByTokenSuccess() {
        UUID engagementLetterId = UUID.randomUUID();
        PublicAccessToken publicAccessToken = PublicAccessToken.builder()
                .id(UUID.randomUUID())
                .token("public-token-123")
                .purpose(TokenPurpose.ACCEPT_ENGAGEMENT)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .maxUses(5)
                .usedCount(1)
                .isActive(true)
                .engagementLetterId(engagementLetterId)
                .customerId(UUID.randomUUID())
                .build();
        EngagementLetter engagementLetter = EngagementLetter.builder()
                .id(engagementLetterId)
                .creationDate(LocalDate.now())
                .discount(15)
                .closingDate(LocalDate.now().plusDays(10))
                .legalProcedures(List.of(LegalProcedure.builder()
                        .title("procedimiento")
                        .budget(BigDecimal.TEN)
                        .legalTasks(List.of("task"))
                        .build()))
                .paymentMethods(List.of(PaymentMethod.builder().description("Todo").percentage("20%").build()))
                .build();
        BDDMockito.given(this.publicAccessTokenPersistence.readByToken("public-token-123")).willReturn(publicAccessToken);
        BDDMockito.given(this.publicAccessTokenPersistence.update(any(PublicAccessToken.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        BDDMockito.given(this.engagementLetterPersistence.readById(eq(engagementLetterId))).willReturn(engagementLetter);

        EngagementLetter response = this.engagementLetterService.readPublicByToken("public-token-123");

        assertThat(response.getId()).isEqualTo(engagementLetterId);
        assertThat(response.getDiscount()).isEqualTo(15);
        assertThat(response.getLegalProcedures()).hasSize(1);

        ArgumentCaptor<PublicAccessToken> captor = ArgumentCaptor.forClass(PublicAccessToken.class);
        verify(this.publicAccessTokenPersistence).update(captor.capture());
        assertThat(captor.getValue().getUsedCount()).isEqualTo(2);
    }

    @Test
    void testReadPublicEngagementLetterWhenTokenDoesNotExist() {
        BDDMockito.given(this.publicAccessTokenPersistence.readByToken("missing-token"))
                .willThrow(new NotFoundException("The PublicAccessToken doesn't exist: missing-token"));

        assertThatThrownBy(() -> this.engagementLetterService.readPublicByToken("missing-token"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("missing-token");
    }

    @Test
    void testReadPublicEngagementLetterWhenTokenIsInactive() {
        BDDMockito.given(this.publicAccessTokenPersistence.readByToken("inactive-token"))
                .willReturn(PublicAccessToken.builder()
                        .token("inactive-token")
                        .purpose(TokenPurpose.ACCEPT_ENGAGEMENT)
                        .expiresAt(LocalDateTime.now().plusDays(1))
                        .maxUses(5)
                        .usedCount(0)
                        .isActive(false)
                        .engagementLetterId(UUID.randomUUID())
                        .build());

        assertThatThrownBy(() -> this.engagementLetterService.readPublicByToken("inactive-token"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("inactive");
        verify(this.publicAccessTokenPersistence, never()).update(any(PublicAccessToken.class));
    }

    @Test
    void testReadPublicEngagementLetterWhenTokenHasExpired() {
        BDDMockito.given(this.publicAccessTokenPersistence.readByToken("expired-token"))
                .willReturn(PublicAccessToken.builder()
                        .token("expired-token")
                        .purpose(TokenPurpose.ACCEPT_ENGAGEMENT)
                        .expiresAt(LocalDateTime.now().minusMinutes(1))
                        .maxUses(5)
                        .usedCount(0)
                        .isActive(true)
                        .engagementLetterId(UUID.randomUUID())
                        .build());

        assertThatThrownBy(() -> this.engagementLetterService.readPublicByToken("expired-token"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("expired");
        verify(this.publicAccessTokenPersistence, never()).update(any(PublicAccessToken.class));
    }

    @Test
    void testReadPublicEngagementLetterWhenTokenExceededMaxUses() {
        BDDMockito.given(this.publicAccessTokenPersistence.readByToken("maxed-token"))
                .willReturn(PublicAccessToken.builder()
                        .token("maxed-token")
                        .purpose(TokenPurpose.ACCEPT_ENGAGEMENT)
                        .expiresAt(LocalDateTime.now().plusDays(1))
                        .maxUses(5)
                        .usedCount(5)
                        .isActive(true)
                        .engagementLetterId(UUID.randomUUID())
                        .build());

        assertThatThrownBy(() -> this.engagementLetterService.readPublicByToken("maxed-token"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("maximum uses");
        verify(this.publicAccessTokenPersistence, never()).update(any(PublicAccessToken.class));
    }
}
