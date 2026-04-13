package es.upm.api.infrastructure.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.upm.api.domain.model.EventType;
import es.upm.api.domain.model.Status;
import es.upm.api.domain.model.UserDto;
import es.upm.api.domain.services.EngagementLetterService;
import es.upm.api.domain.webclients.UserWebClient;
import es.upm.api.infrastructure.dtos.CommentCreateDto;
import es.upm.api.infrastructure.dtos.CommentDeleteDto;
import es.upm.api.infrastructure.dtos.EventCreateDto;
import es.upm.api.infrastructure.dtos.EventUpdateDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class EventResourceIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EngagementLetterService engagementLetterService;

    @MockitoBean
    private UserWebClient userWebClient;

    private UUID engagementLetterId;
    private LocalDateTime eventDate;

    @BeforeEach
    void setUp() {
        engagementLetterId = UUID.randomUUID();
        eventDate = LocalDateTime.now().plusDays(1);
        BDDMockito.given(this.engagementLetterService.readById(any(UUID.class)))
                .willReturn(new es.upm.api.domain.model.EngagementLetter());
        BDDMockito.given(this.userWebClient.readUserByMobile("600000001"))
                .willReturn(UserDto.builder()
                        .id(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0001"))
                        .mobile("600000001")
                        .firstName("Laura")
                        .build());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_admin"})
    void testCreateEventWithoutComments() throws Exception {
        // Arrange
        EventCreateDto eventCreateDto = EventCreateDto.builder()
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Event Title")
                .description("Event Description")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();

        String eventJson = objectMapper.writeValueAsString(eventCreateDto);

        // Act & Assert
        mockMvc.perform(post(EventResource.EVENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.createdDate", notNullValue()))
                .andExpect(jsonPath("$.eventDate", notNullValue()))
                .andExpect(jsonPath("$.type", is("MILESTONE")))
                .andExpect(jsonPath("$.title", is("Event Title")))
                .andExpect(jsonPath("$.description", is("Event Description")))
                .andExpect(jsonPath("$.status", is("PENDING")))
                .andExpect(jsonPath("$.engagementLetterId", is(engagementLetterId.toString())))
                .andExpect(jsonPath("$.comments", hasSize(0)));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_admin"})
    void testCreateEventStartsWithoutComments() throws Exception {
        EventCreateDto eventCreateDto = EventCreateDto.builder()
                .eventDate(eventDate)
                .type(EventType.PHASES)
                .title("Event without Comments")
                .description("Event Description")
                .status(Status.IN_PROGRESS)
                .engagementLetterId(engagementLetterId)
                .build();

        String eventJson = objectMapper.writeValueAsString(eventCreateDto);

        // Act & Assert
        mockMvc.perform(post(EventResource.EVENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.createdDate", notNullValue()))
                .andExpect(jsonPath("$.type", is("PHASES")))
                .andExpect(jsonPath("$.title", is("Event without Comments")))
                .andExpect(jsonPath("$.status", is("IN_PROGRESS")))
                .andExpect(jsonPath("$.comments", hasSize(0)));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_admin"})
    void testCreateEventWithoutTitle_ShouldFail() throws Exception {
        // Arrange
        EventCreateDto eventCreateDto = EventCreateDto.builder()
                .eventDate(eventDate)
                .type(EventType.STANDARD_EVENT)
                .title(null) // Missing title
                .description("Event Description")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();

        String eventJson = objectMapper.writeValueAsString(eventCreateDto);

        // Act & Assert
        mockMvc.perform(post(EventResource.EVENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_admin"})
    void testCreateEventWithoutType_ShouldFail() throws Exception {
        // Arrange
        EventCreateDto eventCreateDto = EventCreateDto.builder()
                .eventDate(eventDate)
                .type(null) // Missing type
                .title("Event Title")
                .description("Event Description")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();

        String eventJson = objectMapper.writeValueAsString(eventCreateDto);

        // Act & Assert
        mockMvc.perform(post(EventResource.EVENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_admin"})
    void testCreateEventWithoutStatus_ShouldFail() throws Exception {
        // Arrange
        EventCreateDto eventCreateDto = EventCreateDto.builder()
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Event Title")
                .description("Event Description")
                .status(null) // Missing status
                .engagementLetterId(engagementLetterId)
                .build();

        String eventJson = objectMapper.writeValueAsString(eventCreateDto);

        // Act & Assert
        mockMvc.perform(post(EventResource.EVENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_admin"})
    void testCreateEventWithoutEngagementLetterId_ShouldFail() throws Exception {
        // Arrange
        EventCreateDto eventCreateDto = EventCreateDto.builder()
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Event Title")
                .description("Event Description")
                .status(Status.PENDING)
                .engagementLetterId(null) // Missing engagement letter ID
                .build();

        String eventJson = objectMapper.writeValueAsString(eventCreateDto);

        // Act & Assert
        mockMvc.perform(post(EventResource.EVENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateEventWithoutAuthentication_ShouldFail() throws Exception {
        // Arrange
        EventCreateDto eventCreateDto = EventCreateDto.builder()
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Event Title")
                .description("Event Description")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();

        String eventJson = objectMapper.writeValueAsString(eventCreateDto);

        // Act & Assert
        mockMvc.perform(post(EventResource.EVENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "manager", authorities = {"ROLE_manager"})
    void testCreateEventWithManagerRole() throws Exception {
        // Arrange
        EventCreateDto eventCreateDto = EventCreateDto.builder()
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Event by Manager")
                .description("Event Description")
                .status(Status.COMPLETED)
                .engagementLetterId(engagementLetterId)
                .build();

        String eventJson = objectMapper.writeValueAsString(eventCreateDto);

        // Act & Assert
        mockMvc.perform(post(EventResource.EVENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("Event by Manager")))
                .andExpect(jsonPath("$.status", is("COMPLETED")));
    }

    @Test
    @WithMockUser(username = "operator", authorities = {"ROLE_operator"})
    void testCreateEventWithOperatorRole() throws Exception {
        // Arrange
        EventCreateDto eventCreateDto = EventCreateDto.builder()
                .eventDate(eventDate)
                .type(EventType.PHASES)
                .title("Event by Operator")
                .description("Event Description")
                .status(Status.IN_PROGRESS)
                .engagementLetterId(engagementLetterId)
                .build();

        String eventJson = objectMapper.writeValueAsString(eventCreateDto);

        // Act & Assert
        mockMvc.perform(post(EventResource.EVENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("Event by Operator")));
    }

    @Test
    @WithMockUser(username = "user", authorities = {"ROLE_user"})
    void testCreateEventWithUnauthorizedRole_ShouldFail() throws Exception {
        // Arrange
        EventCreateDto eventCreateDto = EventCreateDto.builder()
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Event Title")
                .description("Event Description")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();

        String eventJson = objectMapper.writeValueAsString(eventCreateDto);

        // Act & Assert
        // When authenticated but with unauthorized role, Spring Security returns 401
        mockMvc.perform(post(EventResource.EVENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_admin"})
    void testCreateEventAllStatus() throws Exception {
        // Test with CANCELLED status
        EventCreateDto eventCreateDto = EventCreateDto.builder()
                .eventDate(eventDate)
                .type(EventType.STANDARD_EVENT)
                .title("Cancelled Event")
                .description("Event Description")
                .status(Status.CANCELLED)
                .engagementLetterId(engagementLetterId)
                .build();

        String eventJson = objectMapper.writeValueAsString(eventCreateDto);

        // Act & Assert
        mockMvc.perform(post(EventResource.EVENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("CANCELLED")));
    }

    @Test
    @WithMockUser(username = "600000001", authorities = {"ROLE_admin"})
    void testCreateCommentForEvent() throws Exception {
        EventCreateDto eventCreateDto = EventCreateDto.builder()
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Event Title")
                .description("Event Description")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();
        String eventJson = objectMapper.writeValueAsString(eventCreateDto);
        String eventResponse = mockMvc.perform(post(EventResource.EVENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String eventId = objectMapper.readTree(eventResponse).get("id").asText();
        CommentCreateDto commentCreateDto = CommentCreateDto.builder()
                .content("Comentario de seguimiento")
                .build();

        mockMvc.perform(post(EventResource.EVENTS + EventResource.ID_COMMENTS, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentCreateDto)))
                .andExpect(status().isCreated())
                .andExpect(content().string(""));
    }

    @Test
    @WithMockUser(username = "600000001", authorities = {"ROLE_admin"})
    void testCreateCommentForMissingEventShouldFail() throws Exception {
        CommentCreateDto commentCreateDto = CommentCreateDto.builder()
                .content("Comentario")
                .build();

        mockMvc.perform(post(EventResource.EVENTS + EventResource.ID_COMMENTS, UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentCreateDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "600000001", authorities = {"ROLE_admin"})
    void testCreateCommentWithEmptyContentShouldFail() throws Exception {
        EventCreateDto eventCreateDto = EventCreateDto.builder()
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Event Title")
                .description("Event Description")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();
        String eventJson = objectMapper.writeValueAsString(eventCreateDto);
        String eventResponse = mockMvc.perform(post(EventResource.EVENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String eventId = objectMapper.readTree(eventResponse).get("id").asText();
        CommentCreateDto commentCreateDto = CommentCreateDto.builder()
                .content(" ")
                .build();

        mockMvc.perform(post(EventResource.EVENTS + EventResource.ID_COMMENTS, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentCreateDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_admin"})
    void testDeleteEventWithAdmin() throws Exception {
        // Arrange
        EventCreateDto eventCreateDto = EventCreateDto.builder()
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Event to delete")
                .description("Event Description")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();
        String eventJson = objectMapper.writeValueAsString(eventCreateDto);
        String eventResponse = mockMvc.perform(post(EventResource.EVENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String eventId = objectMapper.readTree(eventResponse).get("id").asText();

        // Act & Assert
        mockMvc.perform(delete(EventResource.EVENTS + EventResource.ID_ID, eventId))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    @Test
    @WithMockUser(username = "manager", authorities = {"ROLE_manager"})
    void testDeleteEventWithManager() throws Exception {
        // Arrange
        EventCreateDto eventCreateDto = EventCreateDto.builder()
                .eventDate(eventDate)
                .type(EventType.PHASES)
                .title("Event to delete by manager")
                .status(Status.IN_PROGRESS)
                .engagementLetterId(engagementLetterId)
                .build();
        String eventJson = objectMapper.writeValueAsString(eventCreateDto);
        String eventResponse = mockMvc.perform(post(EventResource.EVENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String eventId = objectMapper.readTree(eventResponse).get("id").asText();

        // Act & Assert
        mockMvc.perform(delete(EventResource.EVENTS + EventResource.ID_ID, eventId))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "operator", authorities = {"ROLE_operator"})
    void testDeleteEventWithOperator() throws Exception {
        // Arrange
        EventCreateDto eventCreateDto = EventCreateDto.builder()
                .eventDate(eventDate)
                .type(EventType.STANDARD_EVENT)
                .title("Event to delete by operator")
                .status(Status.COMPLETED)
                .engagementLetterId(engagementLetterId)
                .build();
        String eventJson = objectMapper.writeValueAsString(eventCreateDto);
        String eventResponse = mockMvc.perform(post(EventResource.EVENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String eventId = objectMapper.readTree(eventResponse).get("id").asText();

        // Act & Assert
        mockMvc.perform(delete(EventResource.EVENTS + EventResource.ID_ID, eventId))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteEventWithoutAuthentication_ShouldFail() throws Exception {
        // Act & Assert
        mockMvc.perform(delete(EventResource.EVENTS + EventResource.ID_ID, UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user", authorities = {"ROLE_user"})
    void testDeleteEventWithUnauthorizedRole_ShouldFail() throws Exception {
        // Act & Assert
        mockMvc.perform(delete(EventResource.EVENTS + EventResource.ID_ID, UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_admin"})
    void testDeleteNonExistentEvent() throws Exception {
        // Act & Assert - Should return 204 NO_CONTENT (idempotent)
        mockMvc.perform(delete(EventResource.EVENTS + EventResource.ID_ID, UUID.randomUUID()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_admin"})
    void testUpdateEventWithAllFields() throws Exception {
        // Arrange - Create event
        EventCreateDto eventCreateDto = EventCreateDto.builder()
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Original title")
                .description("Original description")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();
        String eventJson = objectMapper.writeValueAsString(eventCreateDto);
        String eventResponse = mockMvc.perform(post(EventResource.EVENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String eventId = objectMapper.readTree(eventResponse).get("id").asText();

        // Update with all fields
        EventUpdateDto updateDto = EventUpdateDto.builder()
                .eventDate(eventDate.plusDays(1))
                .type(EventType.PHASES)
                .title("Updated title")
                .description("Updated description")
                .status(Status.IN_PROGRESS)
                .build();

        // Act & Assert
        mockMvc.perform(put(EventResource.EVENTS + EventResource.ID_ID, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(eventId)))
                .andExpect(jsonPath("$.type", is("PHASES")))
                .andExpect(jsonPath("$.title", is("Updated title")))
                .andExpect(jsonPath("$.description", is("Updated description")))
                .andExpect(jsonPath("$.status", is("IN_PROGRESS")));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_admin"})
    void testUpdateEventPartial() throws Exception {
        // Arrange - Create event
        EventCreateDto eventCreateDto = EventCreateDto.builder()
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Original title")
                .description("Original description")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();
        String eventJson = objectMapper.writeValueAsString(eventCreateDto);
        String eventResponse = mockMvc.perform(post(EventResource.EVENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String eventId = objectMapper.readTree(eventResponse).get("id").asText();

        // Update only title (partial update)
        EventUpdateDto updateDto = EventUpdateDto.builder()
                .title("Updated title only")
                .build();

        // Act & Assert
        mockMvc.perform(put(EventResource.EVENTS + EventResource.ID_ID, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Updated title only")))
                .andExpect(jsonPath("$.description", is("Original description")))
                .andExpect(jsonPath("$.type", is("MILESTONE")))
                .andExpect(jsonPath("$.status", is("PENDING")));
    }

    @Test
    @WithMockUser(username = "manager", authorities = {"ROLE_manager"})
    void testUpdateEventWithManagerRole() throws Exception {
        // Arrange - Create event
        EventCreateDto eventCreateDto = EventCreateDto.builder()
                .eventDate(eventDate)
                .type(EventType.STANDARD_EVENT)
                .title("Event title")
                .status(Status.COMPLETED)
                .engagementLetterId(engagementLetterId)
                .build();
        String eventJson = objectMapper.writeValueAsString(eventCreateDto);
        String eventResponse = mockMvc.perform(post(EventResource.EVENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String eventId = objectMapper.readTree(eventResponse).get("id").asText();

        EventUpdateDto updateDto = EventUpdateDto.builder()
                .status(Status.CANCELLED)
                .build();

        // Act & Assert
        mockMvc.perform(put(EventResource.EVENTS + EventResource.ID_ID, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")));
    }

    @Test
    void testUpdateEventWithoutAuthentication_ShouldFail() throws Exception {
        EventUpdateDto updateDto = EventUpdateDto.builder()
                .title("Updated")
                .build();

        // Act & Assert
        mockMvc.perform(put(EventResource.EVENTS + EventResource.ID_ID, UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user", authorities = {"ROLE_user"})
    void testUpdateEventWithUnauthorizedRole_ShouldFail() throws Exception {
        EventUpdateDto updateDto = EventUpdateDto.builder()
                .title("Updated")
                .build();

        // Act & Assert
        mockMvc.perform(put(EventResource.EVENTS + EventResource.ID_ID, UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_admin"})
    void testUpdateNonExistentEvent_ShouldFail() throws Exception {
        EventUpdateDto updateDto = EventUpdateDto.builder()
                .title("Updated")
                .build();

        // Act & Assert
        mockMvc.perform(put(EventResource.EVENTS + EventResource.ID_ID, UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_admin"})
    void testUpdateEventWithEmptyBody() throws Exception {
        // Arrange - Create event
        EventCreateDto eventCreateDto = EventCreateDto.builder()
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Event title")
                .description("Event description")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();
        String eventJson = objectMapper.writeValueAsString(eventCreateDto);
        String eventResponse = mockMvc.perform(post(EventResource.EVENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String eventId = objectMapper.readTree(eventResponse).get("id").asText();

        // Update with empty DTO (no fields to update)
        EventUpdateDto updateDto = EventUpdateDto.builder().build();

        // Act & Assert - Should return 200 OK without changes
        mockMvc.perform(put(EventResource.EVENTS + EventResource.ID_ID, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Event title")))
                .andExpect(jsonPath("$.description", is("Event description")))
                .andExpect(jsonPath("$.type", is("MILESTONE")))
                .andExpect(jsonPath("$.status", is("PENDING")));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_admin"})
    void testReadEventById() throws Exception {
        // Arrange
        EventCreateDto eventCreateDto = EventCreateDto.builder()
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Event to read")
                .description("Event Description")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();
        String eventJson = objectMapper.writeValueAsString(eventCreateDto);
        String eventResponse = mockMvc.perform(post(EventResource.EVENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String eventId = objectMapper.readTree(eventResponse).get("id").asText();

        // Act & Assert
        mockMvc.perform(get(EventResource.EVENTS + EventResource.ID_ID, eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(eventId)))
                .andExpect(jsonPath("$.type", is("MILESTONE")))
                .andExpect(jsonPath("$.title", is("Event to read")))
                .andExpect(jsonPath("$.description", is("Event Description")))
                .andExpect(jsonPath("$.status", is("PENDING")));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_admin"})
    void testReadNonExistentEvent_ShouldFail() throws Exception {
        // Act & Assert
        mockMvc.perform(get(EventResource.EVENTS + EventResource.ID_ID, UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testReadEventWithoutAuthentication_ShouldFail() throws Exception {
        // Act & Assert
        mockMvc.perform(get(EventResource.EVENTS + EventResource.ID_ID, UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_admin"})
    void testFindEventsByEngagementLetterId() throws Exception {
        // Arrange - Create multiple events for the same engagement letter
        EventCreateDto event1Dto = EventCreateDto.builder()
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Event 1")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();
        mockMvc.perform(post(EventResource.EVENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event1Dto)))
                .andExpect(status().isCreated());

        EventCreateDto event2Dto = EventCreateDto.builder()
                .eventDate(eventDate.plusDays(1))
                .type(EventType.PHASES)
                .title("Event 2")
                .status(Status.IN_PROGRESS)
                .engagementLetterId(engagementLetterId)
                .build();
        mockMvc.perform(post(EventResource.EVENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event2Dto)))
                .andExpect(status().isCreated());

        // Act & Assert
        mockMvc.perform(get(EventResource.EVENTS + EventResource.ENGAGEMENT_LETTER_ID, engagementLetterId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", is("Event 1")))
                .andExpect(jsonPath("$[0].status", is("PENDING")))
                .andExpect(jsonPath("$[1].title", is("Event 2")))
                .andExpect(jsonPath("$[1].status", is("IN_PROGRESS")));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_admin"})
    void testFindEventsByEngagementLetterId_Empty() throws Exception {
        // Act & Assert - No events for random engagement letter ID
        mockMvc.perform(get(EventResource.EVENTS + EventResource.ENGAGEMENT_LETTER_ID, UUID.randomUUID()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @WithMockUser(username = "manager", authorities = {"ROLE_manager"})
    void testFindEventsByEngagementLetterIdWithManagerRole() throws Exception {
        // Arrange - Create events
        EventCreateDto eventCreateDto = EventCreateDto.builder()
                .eventDate(eventDate)
                .type(EventType.STANDARD_EVENT)
                .title("Event for manager")
                .status(Status.COMPLETED)
                .engagementLetterId(engagementLetterId)
                .build();
        mockMvc.perform(post(EventResource.EVENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventCreateDto)))
                .andExpect(status().isCreated());

        // Act & Assert
        mockMvc.perform(get(EventResource.EVENTS + EventResource.ENGAGEMENT_LETTER_ID, engagementLetterId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Event for manager")));
    }

    @Test
    void testFindEventsByEngagementLetterIdWithoutAuthentication_ShouldFail() throws Exception {
        // Act & Assert
        mockMvc.perform(get(EventResource.EVENTS + EventResource.ENGAGEMENT_LETTER_ID, engagementLetterId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "600000001", authorities = {"ROLE_admin"})
    void testDeleteNonExistentCommentShouldFail() throws Exception {
        // Arrange - Create event without comments
        EventCreateDto eventCreateDto = EventCreateDto.builder()
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Event without comments")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();
        String eventJson = objectMapper.writeValueAsString(eventCreateDto);
        String eventResponse = mockMvc.perform(post(EventResource.EVENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String eventId = objectMapper.readTree(eventResponse).get("id").asText();

        // Act & Assert - Try to delete non-existent comment
        CommentDeleteDto deleteDto = CommentDeleteDto.builder()
                .authorId(UUID.randomUUID())
                .createdDate(LocalDateTime.now())
                .content("Non-existent comment")
                .build();

        mockMvc.perform(delete(EventResource.EVENTS + EventResource.ID_COMMENTS, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deleteDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "600000001", authorities = {"ROLE_admin"})
    void testDeleteCommentFromNonExistentEventShouldFail() throws Exception {
        // Arrange
        CommentDeleteDto deleteDto = CommentDeleteDto.builder()
                .authorId(UUID.randomUUID())
                .createdDate(LocalDateTime.now())
                .content("Some comment")
                .build();

        // Act & Assert
        mockMvc.perform(delete(EventResource.EVENTS + EventResource.ID_COMMENTS, UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deleteDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "600000001", authorities = {"ROLE_admin"})
    void testDeleteCommentWithInvalidBodyShouldFail() throws Exception {
        // Arrange - Create event
        EventCreateDto eventCreateDto = EventCreateDto.builder()
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Event")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();
        String eventJson = objectMapper.writeValueAsString(eventCreateDto);
        String eventResponse = mockMvc.perform(post(EventResource.EVENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String eventId = objectMapper.readTree(eventResponse).get("id").asText();

        // Act & Assert - Send invalid body (missing required fields)
        String invalidJson = "{}";

        mockMvc.perform(delete(EventResource.EVENTS + EventResource.ID_COMMENTS, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "600000002", authorities = {"ROLE_admin"})
    void testDeleteCommentAsDifferentUserShouldFail() throws Exception {

        // Arrange - Create event
        EventCreateDto eventCreateDto = EventCreateDto.builder()
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Event")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();

        String eventJson = objectMapper.writeValueAsString(eventCreateDto);

        String eventResponse = mockMvc.perform(post(EventResource.EVENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String eventId = objectMapper.readTree(eventResponse).get("id").asText();

        // 🔥 MOCK USER 1 (autor del comentario)
        UUID user1Id = UUID.randomUUID();
        when(userWebClient.readUserByMobile("600000001"))
                .thenReturn(UserDto.builder().id(user1Id).build());

        // 🔥 MOCK USER 2 (usuario que intenta borrar)
        UUID user2Id = UUID.randomUUID();
        when(userWebClient.readUserByMobile("600000002"))
                .thenReturn(UserDto.builder().id(user2Id).build());

        // Create comment as user1
        CommentCreateDto commentCreateDto = CommentCreateDto.builder()
                .content("Comment by user1")
                .build();

        mockMvc.perform(post(EventResource.EVENTS + EventResource.ID_COMMENTS, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentCreateDto))
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("600000001")
                                .roles("admin")))
                .andExpect(status().isCreated());

        // Read event to get comment
        String eventWithCommentResponse = mockMvc.perform(get(EventResource.EVENTS + EventResource.ID_ID, eventId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var commentNode = objectMapper.readTree(eventWithCommentResponse)
                .get("comments")
                .get(0);

        UUID authorId = UUID.fromString(commentNode.get("authorId").asText());

        LocalDateTime createdDate = LocalDateTime.parse(commentNode.get("createdDate").asText())
                .withNano(0);

        String content = commentNode.get("content").asText();

        CommentDeleteDto deleteDto = CommentDeleteDto.builder()
                .authorId(authorId)
                .createdDate(createdDate)
                .content(content)
                .build();

        // Act & Assert - delete as different user
        mockMvc.perform(delete(EventResource.EVENTS + EventResource.ID_COMMENTS, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deleteDto)))
                .andExpect(status().isForbidden());
    }


    @Test
    @WithMockUser(username = "600000001", authorities = {"ROLE_admin"})
    void testDeleteCommentMultipleCommentsRemovesOnlyOne() throws Exception {
        // Arrange - Create event with multiple comments
        EventCreateDto eventCreateDto = EventCreateDto.builder()
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Event with multiple comments")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();
        String eventJson = objectMapper.writeValueAsString(eventCreateDto);
        String eventResponse = mockMvc.perform(post(EventResource.EVENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String eventId = objectMapper.readTree(eventResponse).get("id").asText();

        // Add first comment
        CommentCreateDto comment1 = CommentCreateDto.builder()
                .content("First comment")
                .build();
        mockMvc.perform(post(EventResource.EVENTS + EventResource.ID_COMMENTS, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(comment1)))
                .andExpect(status().isCreated());

        // Wait a bit to ensure different timestamps
        try {
            Thread.sleep(1100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Add second comment
        CommentCreateDto comment2 = CommentCreateDto.builder()
                .content("Second comment")
                .build();
        mockMvc.perform(post(EventResource.EVENTS + EventResource.ID_COMMENTS, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(comment2)))
                .andExpect(status().isCreated());

        // Read event to get first comment details
        String eventWithCommentsResponse = mockMvc.perform(get(EventResource.EVENTS + EventResource.ID_ID, eventId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var commentsNode = objectMapper.readTree(eventWithCommentsResponse).get("comments");
        assertThat(commentsNode.size(), is(2));

        var firstComment = commentsNode.get(0);
        UUID authorId = UUID.fromString(firstComment.get("authorId").asText());
        String createdDateStr = firstComment.get("createdDate").asText();
        LocalDateTime createdDate = LocalDateTime.parse(createdDateStr);
        String content = firstComment.get("content").asText();

        // Act - Delete first comment
        CommentDeleteDto deleteDto = CommentDeleteDto.builder()
                .authorId(authorId)
                .createdDate(createdDate)
                .content(content)
                .build();

        mockMvc.perform(delete(EventResource.EVENTS + EventResource.ID_COMMENTS, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deleteDto)))
                .andExpect(status().isNoContent());

        // Assert - Only second comment should remain
        String eventAfterDeleteResponse = mockMvc.perform(get(EventResource.EVENTS + EventResource.ID_ID, eventId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var commentsAfterDelete = objectMapper.readTree(eventAfterDeleteResponse).get("comments");
        assertThat(commentsAfterDelete.size(), is(1));
        assertThat(commentsAfterDelete.get(0).get("content").asText(), is("Second comment"));
    }

    @Test
    @WithMockUser(username = "600000001", authorities = {"ROLE_manager"})
    void testDeleteCommentWithManagerRole() throws Exception {
        // Arrange - Create event and comment
        EventCreateDto eventCreateDto = EventCreateDto.builder()
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Event")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();

        String eventJson = objectMapper.writeValueAsString(eventCreateDto);

        String eventResponse = mockMvc.perform(post(EventResource.EVENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String eventId = objectMapper.readTree(eventResponse).get("id").asText();

        // Create comment
        CommentCreateDto commentCreateDto = CommentCreateDto.builder()
                .content("Test comment")
                .build();

        mockMvc.perform(post(EventResource.EVENTS + EventResource.ID_COMMENTS, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentCreateDto)))
                .andExpect(status().isCreated());

        // Read to get comment details
        String eventWithCommentResponse = mockMvc.perform(get(EventResource.EVENTS + EventResource.ID_ID, eventId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var commentNode = objectMapper.readTree(eventWithCommentResponse)
                .get("comments")
                .get(0);

        UUID authorId = UUID.fromString(commentNode.get("authorId").asText());

        //  truncar a segundos
        LocalDateTime createdDate = LocalDateTime.parse(commentNode.get("createdDate").asText())
                .withNano(0);

        String content = commentNode.get("content").asText();

        CommentDeleteDto deleteDto = CommentDeleteDto.builder()
                .authorId(authorId)
                .createdDate(createdDate)
                .content(content)
                .build();

        // Act & Assert
        mockMvc.perform(delete(EventResource.EVENTS + EventResource.ID_COMMENTS, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deleteDto)))
                .andExpect(status().isNoContent());
    }


    @Test
    void testDeleteCommentWithoutAuthenticationShouldFail() throws Exception {
        // Arrange
        CommentDeleteDto deleteDto = CommentDeleteDto.builder()
                .authorId(UUID.randomUUID())
                .createdDate(LocalDateTime.now())
                .content("Test")
                .build();

        // Act & Assert
        mockMvc.perform(delete(EventResource.EVENTS + EventResource.ID_COMMENTS, UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deleteDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user", authorities = {"ROLE_user"})
    void testDeleteCommentWithUnauthorizedRoleShouldFail() throws Exception {
        // Arrange
        CommentDeleteDto deleteDto = CommentDeleteDto.builder()
                .authorId(UUID.randomUUID())
                .createdDate(LocalDateTime.now())
                .content("Test")
                .build();

        // Act & Assert
        mockMvc.perform(delete(EventResource.EVENTS + EventResource.ID_COMMENTS, UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deleteDto)))
                .andExpect(status().isUnauthorized());
    }
    @Test
    @WithMockUser(username = "600000001", authorities = {"ROLE_admin"})
    void testDeleteCommentWithMissingAuthorIdShouldFail() throws Exception {
        // Arrange - Create event
        EventCreateDto eventCreateDto = EventCreateDto.builder()
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Event")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();
        String eventJson = objectMapper.writeValueAsString(eventCreateDto);
        String eventResponse = mockMvc.perform(post(EventResource.EVENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String eventId = objectMapper.readTree(eventResponse).get("id").asText();

        // Act & Assert - Missing authorId
        String invalidJson = "{\"createdDate\":\"2025-01-15T10:30:00\",\"content\":\"Test\"}";

        mockMvc.perform(delete(EventResource.EVENTS + EventResource.ID_COMMENTS, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_admin"})
    void testGetTimelineEventsDescendingOrder() throws Exception {
        UUID engagementId = UUID.randomUUID();
        BDDMockito.given(this.engagementLetterService.readById(any(UUID.class)))
                .willReturn(new es.upm.api.domain.model.EngagementLetter());

        EventCreateDto event1 = EventCreateDto.builder()
                .eventDate(LocalDateTime.of(2026, 4, 15, 10, 0, 0))
                .type(EventType.MILESTONE)
                .title("First Event")
                .description("Description 1")
                .status(Status.PENDING)
                .engagementLetterId(engagementId)
                .build();

        EventCreateDto event2 = EventCreateDto.builder()
                .eventDate(LocalDateTime.of(2026, 4, 20, 14, 30, 0))
                .type(EventType.PHASES)
                .title("Second Event")
                .description("Description 2")
                .status(Status.IN_PROGRESS)
                .engagementLetterId(engagementId)
                .build();

        EventCreateDto event3 = EventCreateDto.builder()
                .eventDate(LocalDateTime.of(2026, 4, 18, 9, 15, 0))
                .type(EventType.STANDARD_EVENT)
                .title("Third Event")
                .description("Description 3")
                .status(Status.COMPLETED)
                .engagementLetterId(engagementId)
                .build();

        String event1Json = objectMapper.writeValueAsString(event1);
        String event2Json = objectMapper.writeValueAsString(event2);
        String event3Json = objectMapper.writeValueAsString(event3);

        mockMvc.perform(post(EventResource.EVENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(event1Json))
                .andExpect(status().isCreated());

        mockMvc.perform(post(EventResource.EVENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(event2Json))
                .andExpect(status().isCreated());

        mockMvc.perform(post(EventResource.EVENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(event3Json))
                .andExpect(status().isCreated());

        String timelineUrl = EventResource.EVENTS + "/engagement-letter/" + engagementId + "/timeline-events";

        mockMvc.perform(get(timelineUrl)
                        .param("ascending", "false")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].title", is("Second Event")))
                .andExpect(jsonPath("$[0].date", is("2026-04-20T14:30:00")))
                .andExpect(jsonPath("$[0].type", is("PHASES")))
                .andExpect(jsonPath("$[0].status", is("IN_PROGRESS")))
                .andExpect(jsonPath("$[1].title", is("Third Event")))
                .andExpect(jsonPath("$[1].date", is("2026-04-18T09:15:00")))
                .andExpect(jsonPath("$[2].title", is("First Event")))
                .andExpect(jsonPath("$[2].date", is("2026-04-15T10:00:00")));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_admin"})
    void testGetTimelineEventsAscendingOrder() throws Exception {
        UUID engagementId = UUID.randomUUID();
        BDDMockito.given(this.engagementLetterService.readById(any(UUID.class)))
                .willReturn(new es.upm.api.domain.model.EngagementLetter());

        EventCreateDto event1 = EventCreateDto.builder()
                .eventDate(LocalDateTime.of(2026, 4, 25, 11, 0, 0))
                .type(EventType.MILESTONE)
                .title("Later Event")
                .status(Status.PENDING)
                .engagementLetterId(engagementId)
                .build();

        EventCreateDto event2 = EventCreateDto.builder()
                .eventDate(LocalDateTime.of(2026, 4, 10, 9, 0, 0))
                .type(EventType.PHASES)
                .title("Earlier Event")
                .status(Status.COMPLETED)
                .engagementLetterId(engagementId)
                .build();

        String event1Json = objectMapper.writeValueAsString(event1);
        String event2Json = objectMapper.writeValueAsString(event2);

        mockMvc.perform(post(EventResource.EVENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(event1Json))
                .andExpect(status().isCreated());

        mockMvc.perform(post(EventResource.EVENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(event2Json))
                .andExpect(status().isCreated());

        String timelineUrl = EventResource.EVENTS + "/engagement-letter/" + engagementId + "/timeline-events";

        mockMvc.perform(get(timelineUrl)
                        .param("ascending", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", is("Earlier Event")))
                .andExpect(jsonPath("$[0].date", is("2026-04-10T09:00:00")))
                .andExpect(jsonPath("$[1].title", is("Later Event")))
                .andExpect(jsonPath("$[1].date", is("2026-04-25T11:00:00")));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_admin"})
    void testGetTimelineEventsDefaultOrderIsDescending() throws Exception {
        UUID engagementId = UUID.randomUUID();
        BDDMockito.given(this.engagementLetterService.readById(any(UUID.class)))
                .willReturn(new es.upm.api.domain.model.EngagementLetter());

        EventCreateDto event1 = EventCreateDto.builder()
                .eventDate(LocalDateTime.of(2026, 4, 15, 10, 0, 0))
                .type(EventType.MILESTONE)
                .title("Older Event")
                .status(Status.PENDING)
                .engagementLetterId(engagementId)
                .build();

        EventCreateDto event2 = EventCreateDto.builder()
                .eventDate(LocalDateTime.of(2026, 4, 20, 14, 0, 0))
                .type(EventType.PHASES)
                .title("Newer Event")
                .status(Status.IN_PROGRESS)
                .engagementLetterId(engagementId)
                .build();

        String event1Json = objectMapper.writeValueAsString(event1);
        String event2Json = objectMapper.writeValueAsString(event2);

        mockMvc.perform(post(EventResource.EVENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(event1Json))
                .andExpect(status().isCreated());

        mockMvc.perform(post(EventResource.EVENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(event2Json))
                .andExpect(status().isCreated());

        String timelineUrl = EventResource.EVENTS + "/engagement-letter/" + engagementId + "/timeline-events";

        mockMvc.perform(get(timelineUrl)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", is("Newer Event")))
                .andExpect(jsonPath("$[1].title", is("Older Event")));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_admin"})
    void testGetTimelineEventsEmptyList() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        String timelineUrl = EventResource.EVENTS + "/engagement-letter/" + nonExistentId + "/timeline-events";

        mockMvc.perform(get(timelineUrl)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_admin"})
    void testGetTimelineEventsReturnsOnlyRequiredFields() throws Exception {
        UUID engagementId = UUID.randomUUID();
        BDDMockito.given(this.engagementLetterService.readById(any(UUID.class)))
                .willReturn(new es.upm.api.domain.model.EngagementLetter());

        EventCreateDto event = EventCreateDto.builder()
                .eventDate(LocalDateTime.of(2026, 4, 15, 10, 0, 0))
                .type(EventType.MILESTONE)
                .title("Timeline Event")
                .description("Timeline Description")
                .status(Status.PENDING)
                .engagementLetterId(engagementId)
                .build();

        String eventJson = objectMapper.writeValueAsString(event);
        mockMvc.perform(post(EventResource.EVENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson))
                .andExpect(status().isCreated());

        String timelineUrl = EventResource.EVENTS + "/engagement-letter/" + engagementId + "/timeline-events";

        mockMvc.perform(get(timelineUrl)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", notNullValue()))
                .andExpect(jsonPath("$[0].date", is("2026-04-15T10:00:00")))
                .andExpect(jsonPath("$[0].type", is("MILESTONE")))
                .andExpect(jsonPath("$[0].title", is("Timeline Event")))
                .andExpect(jsonPath("$[0].description", is("Timeline Description")))
                .andExpect(jsonPath("$[0].status", is("PENDING")))
                .andExpect(jsonPath("$[0].createdDate").doesNotExist())
                .andExpect(jsonPath("$[0].engagementLetterId").doesNotExist())
                .andExpect(jsonPath("$[0].comments").doesNotExist());
    }

    @Test
    @WithMockUser(username = "user", authorities = {"ROLE_user"})
    void testGetTimelineEventsUnauthorizedForNonAdmin() throws Exception {
        UUID engagementId = UUID.randomUUID();
        String timelineUrl = EventResource.EVENTS + "/engagement-letter/" + engagementId + "/timeline-events";

        mockMvc.perform(get(timelineUrl)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }



}






