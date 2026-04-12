package es.upm.api.infrastructure.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.upm.api.domain.model.EventType;
import es.upm.api.domain.model.Status;
import es.upm.api.domain.model.UserDto;
import es.upm.api.domain.services.EngagementLetterService;
import es.upm.api.domain.webclients.UserWebClient;
import es.upm.api.infrastructure.dtos.CommentCreateDto;
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

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
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
}






