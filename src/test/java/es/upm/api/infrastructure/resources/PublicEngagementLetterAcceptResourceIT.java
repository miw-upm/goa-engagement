package es.upm.api.infrastructure.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.upm.api.domain.exceptions.BadRequestException;
import es.upm.api.domain.exceptions.NotFoundException;
import es.upm.api.domain.model.AcceptanceEngagement;
import es.upm.api.domain.model.EngagementLetter;
import es.upm.api.domain.services.EngagementLetterService;
import es.upm.api.infrastructure.dtos.PublicEngagementLetterAcceptRequestDto;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class PublicEngagementLetterAcceptResourceIT {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EngagementLetterService engagementLetterService;

    @Test
    void testAcceptByToken() throws Exception {
        UUID engagementLetterId = UUID.randomUUID();
        LocalDateTime signatureDate = LocalDateTime.of(2026, 4, 9, 10, 30, 0);
        BDDMockito.given(this.engagementLetterService.acceptPublicByToken(eq("accept-token-123")))
                .willReturn(EngagementLetter.builder()
                        .id(engagementLetterId)
                        .acceptanceEngagements(List.of(AcceptanceEngagement.builder()
                                .signatureDate(signatureDate)
                                .build()))
                        .build());

        String body = this.objectMapper.writeValueAsString(
                PublicEngagementLetterAcceptRequestDto.builder().token("accept-token-123").build()
        );

        this.mockMvc.perform(post(PublicEngagementLetterResource.PUBLIC_ENGAGEMENT_LETTERS + PublicEngagementLetterResource.ACCEPT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.engagementLetterId").value(engagementLetterId.toString()))
                .andExpect(jsonPath("$.signatureDate").value("2026-04-09T10:30:00"));
    }

    @Test
    void testAcceptByTokenWhenTokenDoesNotExist() throws Exception {
        BDDMockito.given(this.engagementLetterService.acceptPublicByToken(eq("missing-token")))
                .willThrow(new NotFoundException("The PublicAccessToken doesn't exist: missing-token"));

        String body = this.objectMapper.writeValueAsString(
                PublicEngagementLetterAcceptRequestDto.builder().token("missing-token").build()
        );

        this.mockMvc.perform(post(PublicEngagementLetterResource.PUBLIC_ENGAGEMENT_LETTERS + PublicEngagementLetterResource.ACCEPT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void testAcceptByTokenWhenAlreadyAccepted() throws Exception {
        BDDMockito.given(this.engagementLetterService.acceptPublicByToken(eq("accepted-token")))
                .willThrow(new BadRequestException("Cannot accept engagement letter: engagement letter has already been accepted"));

        String body = this.objectMapper.writeValueAsString(
                PublicEngagementLetterAcceptRequestDto.builder().token("accepted-token").build()
        );

        this.mockMvc.perform(post(PublicEngagementLetterResource.PUBLIC_ENGAGEMENT_LETTERS + PublicEngagementLetterResource.ACCEPT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("already been accepted")));
    }

    @Test
    void testAcceptByTokenWhenTokenIsInactive() throws Exception {
        BDDMockito.given(this.engagementLetterService.acceptPublicByToken(eq("inactive-token")))
                .willThrow(new BadRequestException("Cannot accept engagement letter: public access token is inactive"));

        String body = this.objectMapper.writeValueAsString(
                PublicEngagementLetterAcceptRequestDto.builder().token("inactive-token").build()
        );

        this.mockMvc.perform(post(PublicEngagementLetterResource.PUBLIC_ENGAGEMENT_LETTERS + PublicEngagementLetterResource.ACCEPT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("inactive")));
    }

    @Test
    void testAcceptByTokenWhenTokenIsMissingInRequest() throws Exception {
        String body = this.objectMapper.writeValueAsString(
                PublicEngagementLetterAcceptRequestDto.builder().token(" ").build()
        );

        this.mockMvc.perform(post(PublicEngagementLetterResource.PUBLIC_ENGAGEMENT_LETTERS + PublicEngagementLetterResource.ACCEPT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }
}
