package es.upm.api.domain.services;

import es.upm.api.domain.exceptions.BadRequestException;
import es.upm.api.domain.exceptions.NotFoundException;
import es.upm.api.domain.model.EngagementLetter;
import es.upm.api.domain.model.PublicAccessToken;
import es.upm.api.domain.model.TokenPurpose;
import es.upm.api.domain.model.UserDto;
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

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
class EngagementLetterPublicAccessTokenServiceIT {

    @Autowired
    private EngagementLetterService engagementLetterService;

    @MockitoBean
    private EngagementLetterPersistence engagementLetterPersistence;
    @MockitoBean
    private PublicAccessTokenPersistence publicAccessTokenPersistence;
    @MockitoBean
    private UserWebClient userWebClient;

    @Test
    void testCreatePublicAccessTokenSuccess() {
        UUID engagementLetterId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        EngagementLetter engagementLetter = EngagementLetter.builder()
                .id(engagementLetterId)
                .owner(UserDto.builder().id(customerId).mobile("666666000").firstName("mock").build())
                .build();
        BDDMockito.given(this.engagementLetterPersistence.readById(eq(engagementLetterId))).willReturn(engagementLetter);
        BDDMockito.given(this.publicAccessTokenPersistence.create(any(PublicAccessToken.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        PublicAccessToken created = this.engagementLetterService.createPublicAccessToken(engagementLetterId);

        assertThat(created)
                .isNotNull()
                .satisfies(publicAccessToken -> {
                    assertThat(publicAccessToken.getPurpose()).isEqualTo(TokenPurpose.ACCEPT_ENGAGEMENT);
                    assertThat(publicAccessToken.getUsedCount()).isZero();
                    assertThat(publicAccessToken.getIsActive()).isTrue();
                    assertThat(publicAccessToken.getMaxUses()).isEqualTo(5);
                    assertThat(publicAccessToken.getEngagementLetterId()).isEqualTo(engagementLetterId);
                    assertThat(publicAccessToken.getCustomerId()).isEqualTo(customerId);
                    assertThat(publicAccessToken.getToken()).isNotBlank();
                    assertThat(publicAccessToken.getExpiresAt()).isAfter(LocalDateTime.now().plusDays(4));
                });

        ArgumentCaptor<PublicAccessToken> captor = ArgumentCaptor.forClass(PublicAccessToken.class);
        verify(this.publicAccessTokenPersistence).create(captor.capture());
        assertThat(captor.getValue().getCustomerId()).isEqualTo(customerId);
    }

    @Test
    void testCreatePublicAccessTokenWhenOwnerMissing() {
        UUID engagementLetterId = UUID.randomUUID();
        BDDMockito.given(this.engagementLetterPersistence.readById(eq(engagementLetterId)))
                .willReturn(EngagementLetter.builder().id(engagementLetterId).owner(null).build());

        assertThatThrownBy(() -> this.engagementLetterService.createPublicAccessToken(engagementLetterId))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("engagement letter owner is required");
    }

    @Test
    void testCreatePublicAccessTokenWhenEngagementLetterDoesNotExist() {
        UUID engagementLetterId = UUID.randomUUID();
        BDDMockito.given(this.engagementLetterPersistence.readById(eq(engagementLetterId)))
                .willThrow(new NotFoundException("The EngagementLetter ID doesn't exist: " + engagementLetterId));

        assertThatThrownBy(() -> this.engagementLetterService.createPublicAccessToken(engagementLetterId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(engagementLetterId.toString());
    }
}
