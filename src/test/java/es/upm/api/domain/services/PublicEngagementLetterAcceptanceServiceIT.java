package es.upm.api.domain.services;

import es.upm.api.domain.exceptions.BadRequestException;
import es.upm.api.domain.exceptions.NotFoundException;
import es.upm.api.domain.model.AcceptanceEngagement;
import es.upm.api.domain.model.AcceptanceEvidence;
import es.upm.api.domain.model.EngagementLetter;
import es.upm.api.domain.model.PublicAccessToken;
import es.upm.api.domain.model.TokenPurpose;
import es.upm.api.domain.persistence.AcceptanceEvidencePersistence;
import es.upm.api.domain.persistence.EngagementLetterPersistence;
import es.upm.api.domain.persistence.PublicAccessTokenPersistence;
import es.upm.api.domain.webclients.UserWebClient;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

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
class PublicEngagementLetterAcceptanceServiceIT {

    @Autowired
    private EngagementLetterService engagementLetterService;

    private static final String IP_ADDRESS = "203.0.113.10";
    private static final String USER_AGENT = "JUnit-UA/1.0";

    @MockitoBean
    private EngagementLetterPersistence engagementLetterPersistence;
    @MockitoBean
    private PublicAccessTokenPersistence publicAccessTokenPersistence;
    @MockitoBean
    private AcceptanceEvidencePersistence acceptanceEvidencePersistence;
    @MockitoBean
    private UserWebClient userWebClient;

    @Test
    void testAcceptPublicEngagementLetterSuccess() {
        UUID engagementLetterId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID tokenId = UUID.randomUUID();
        PublicAccessToken publicAccessToken = PublicAccessToken.builder()
                .id(tokenId)
                .token("accept-token-123")
                .purpose(TokenPurpose.ACCEPT_ENGAGEMENT)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .maxUses(5)
                .usedCount(1)
                .isActive(true)
                .engagementLetterId(engagementLetterId)
                .customerId(customerId)
                .build();
        EngagementLetter engagementLetter = EngagementLetter.builder()
                .id(engagementLetterId)
                .creationDate(LocalDate.now())
                .acceptanceEngagements(null)
                .build();
        BDDMockito.given(this.publicAccessTokenPersistence.readByToken("accept-token-123")).willReturn(publicAccessToken);
        BDDMockito.given(this.engagementLetterPersistence.readById(eq(engagementLetterId))).willReturn(engagementLetter);
        BDDMockito.given(this.publicAccessTokenPersistence.update(any(PublicAccessToken.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        BDDMockito.given(this.acceptanceEvidencePersistence.create(any(AcceptanceEvidence.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        EngagementLetter response = this.engagementLetterService.acceptPublicByToken("accept-token-123", IP_ADDRESS, USER_AGENT);

        assertThat(response.getId()).isEqualTo(engagementLetterId);
        assertThat(response.getAcceptanceEngagements()).hasSize(1);
        assertThat(response.getAcceptanceEngagements().get(0).getSignatureDate()).isNotNull();
        assertThat(response.getAcceptanceEngagements().get(0).getSigner().getId()).isEqualTo(customerId);

        ArgumentCaptor<EngagementLetter> engagementCaptor = ArgumentCaptor.forClass(EngagementLetter.class);
        verify(this.engagementLetterPersistence).update(eq(engagementLetterId), engagementCaptor.capture());
        assertThat(engagementCaptor.getValue().getAcceptanceEngagements()).hasSize(1);

        ArgumentCaptor<PublicAccessToken> tokenCaptor = ArgumentCaptor.forClass(PublicAccessToken.class);
        verify(this.publicAccessTokenPersistence).update(tokenCaptor.capture());
        assertThat(tokenCaptor.getValue().getUsedCount()).isEqualTo(2);
        assertThat(tokenCaptor.getValue().getIsActive()).isFalse();

        ArgumentCaptor<AcceptanceEvidence> evidenceCaptor = ArgumentCaptor.forClass(AcceptanceEvidence.class);
        verify(this.acceptanceEvidencePersistence).create(evidenceCaptor.capture());
        AcceptanceEvidence evidence = evidenceCaptor.getValue();
        assertThat(evidence.getId()).isNotNull();
        assertThat(evidence.getEngagementLetterId()).isEqualTo(engagementLetterId);
        assertThat(evidence.getAcceptedAt()).isNotNull();
        assertThat(evidence.getAcceptedAt()).isEqualTo(response.getAcceptanceEngagements().get(0).getSignatureDate());
        assertThat(evidence.getIpAddress()).isEqualTo(IP_ADDRESS);
        assertThat(evidence.getUserAgent()).isEqualTo(USER_AGENT);
        assertThat(evidence.getMethod()).isEqualTo(EngagementLetterService.ACCEPT_METHOD_PUBLIC_TOKEN);
        assertThat(evidence.getTokenId()).isEqualTo(tokenId);
    }

    @Test
    void testAcceptPublicEngagementLetterWhenTokenDoesNotExist() {
        BDDMockito.given(this.publicAccessTokenPersistence.readByToken("missing-token"))
                .willThrow(new NotFoundException("The PublicAccessToken doesn't exist: missing-token"));

        assertThatThrownBy(() -> this.engagementLetterService.acceptPublicByToken("missing-token", IP_ADDRESS, USER_AGENT))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("missing-token");
        verify(this.acceptanceEvidencePersistence, never()).create(any(AcceptanceEvidence.class));
    }

    @Test
    void testAcceptPublicEngagementLetterWhenTokenIsInactive() {
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

        assertThatThrownBy(() -> this.engagementLetterService.acceptPublicByToken("inactive-token", IP_ADDRESS, USER_AGENT))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("inactive");
        verify(this.engagementLetterPersistence, never()).update(any(UUID.class), any(EngagementLetter.class));
        verify(this.publicAccessTokenPersistence, never()).update(any(PublicAccessToken.class));
        verify(this.acceptanceEvidencePersistence, never()).create(any(AcceptanceEvidence.class));
    }

    @Test
    void testAcceptPublicEngagementLetterWhenAlreadyAccepted() {
        UUID engagementLetterId = UUID.randomUUID();
        BDDMockito.given(this.publicAccessTokenPersistence.readByToken("accepted-token"))
                .willReturn(PublicAccessToken.builder()
                        .token("accepted-token")
                        .purpose(TokenPurpose.ACCEPT_ENGAGEMENT)
                        .expiresAt(LocalDateTime.now().plusDays(1))
                        .maxUses(5)
                        .usedCount(0)
                        .isActive(true)
                        .engagementLetterId(engagementLetterId)
                        .build());
        BDDMockito.given(this.engagementLetterPersistence.readById(eq(engagementLetterId)))
                .willReturn(EngagementLetter.builder()
                        .id(engagementLetterId)
                        .acceptanceEngagements(List.of(AcceptanceEngagement.builder()
                                .signatureDate(LocalDateTime.now().minusDays(1))
                                .build()))
                        .build());

        assertThatThrownBy(() -> this.engagementLetterService.acceptPublicByToken("accepted-token", IP_ADDRESS, USER_AGENT))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already been accepted");
        verify(this.engagementLetterPersistence, never()).update(any(UUID.class), any(EngagementLetter.class));
        verify(this.publicAccessTokenPersistence, never()).update(any(PublicAccessToken.class));
        verify(this.acceptanceEvidencePersistence, never()).create(any(AcceptanceEvidence.class));
    }

    @Test
    void testAcceptPublicEngagementLetterWhenClosed() {
        UUID engagementLetterId = UUID.randomUUID();
        BDDMockito.given(this.publicAccessTokenPersistence.readByToken("closed-token"))
                .willReturn(PublicAccessToken.builder()
                        .token("closed-token")
                        .purpose(TokenPurpose.ACCEPT_ENGAGEMENT)
                        .expiresAt(LocalDateTime.now().plusDays(1))
                        .maxUses(5)
                        .usedCount(0)
                        .isActive(true)
                        .engagementLetterId(engagementLetterId)
                        .build());
        BDDMockito.given(this.engagementLetterPersistence.readById(eq(engagementLetterId)))
                .willReturn(EngagementLetter.builder()
                        .id(engagementLetterId)
                        .closingDate(LocalDate.now())
                        .build());

        assertThatThrownBy(() -> this.engagementLetterService.acceptPublicByToken("closed-token", IP_ADDRESS, USER_AGENT))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("closed");
        verify(this.engagementLetterPersistence, never()).update(any(UUID.class), any(EngagementLetter.class));
        verify(this.publicAccessTokenPersistence, never()).update(any(PublicAccessToken.class));
        verify(this.acceptanceEvidencePersistence, never()).create(any(AcceptanceEvidence.class));
    }

    @Test
    void testAcceptPublicEngagementLetterWhenTokenHasExpired() {
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

        assertThatThrownBy(() -> this.engagementLetterService.acceptPublicByToken("expired-token", IP_ADDRESS, USER_AGENT))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("expired");
        verify(this.engagementLetterPersistence, never()).update(any(UUID.class), any(EngagementLetter.class));
        verify(this.publicAccessTokenPersistence, never()).update(any(PublicAccessToken.class));
        verify(this.acceptanceEvidencePersistence, never()).create(any(AcceptanceEvidence.class));
    }

    @Test
    void testAcceptPublicEngagementLetterWhenTokenExceededMaxUses() {
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

        assertThatThrownBy(() -> this.engagementLetterService.acceptPublicByToken("maxed-token", IP_ADDRESS, USER_AGENT))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("maximum uses");
        verify(this.engagementLetterPersistence, never()).update(any(UUID.class), any(EngagementLetter.class));
        verify(this.publicAccessTokenPersistence, never()).update(any(PublicAccessToken.class));
        verify(this.acceptanceEvidencePersistence, never()).create(any(AcceptanceEvidence.class));
    }

    @Test
    void testAcceptPublicEngagementLetterWhenTokenPurposeIsInvalid() {
        BDDMockito.given(this.publicAccessTokenPersistence.readByToken("wrong-purpose-token"))
                .willReturn(PublicAccessToken.builder()
                        .token("wrong-purpose-token")
                        .purpose(null)
                        .expiresAt(LocalDateTime.now().plusDays(1))
                        .maxUses(5)
                        .usedCount(0)
                        .isActive(true)
                        .engagementLetterId(UUID.randomUUID())
                        .build());

        assertThatThrownBy(() -> this.engagementLetterService.acceptPublicByToken("wrong-purpose-token", IP_ADDRESS, USER_AGENT))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("purpose");
        verify(this.engagementLetterPersistence, never()).update(any(UUID.class), any(EngagementLetter.class));
        verify(this.publicAccessTokenPersistence, never()).update(any(PublicAccessToken.class));
        verify(this.acceptanceEvidencePersistence, never()).create(any(AcceptanceEvidence.class));
    }

    @Test
    void testAcceptPublicEngagementLetterWhenEngagementLetterDoesNotExist() {
        UUID engagementLetterId = UUID.randomUUID();
        BDDMockito.given(this.publicAccessTokenPersistence.readByToken("orphan-token"))
                .willReturn(PublicAccessToken.builder()
                        .token("orphan-token")
                        .purpose(TokenPurpose.ACCEPT_ENGAGEMENT)
                        .expiresAt(LocalDateTime.now().plusDays(1))
                        .maxUses(5)
                        .usedCount(0)
                        .isActive(true)
                        .engagementLetterId(engagementLetterId)
                        .build());
        BDDMockito.given(this.engagementLetterPersistence.readById(eq(engagementLetterId)))
                .willThrow(new NotFoundException("The EngagementLetter ID doesn't exist: " + engagementLetterId));

        assertThatThrownBy(() -> this.engagementLetterService.acceptPublicByToken("orphan-token", IP_ADDRESS, USER_AGENT))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(engagementLetterId.toString());
        verify(this.engagementLetterPersistence, never()).update(any(UUID.class), any(EngagementLetter.class));
        verify(this.publicAccessTokenPersistence, never()).update(any(PublicAccessToken.class));
        verify(this.acceptanceEvidencePersistence, never()).create(any(AcceptanceEvidence.class));
    }
}
