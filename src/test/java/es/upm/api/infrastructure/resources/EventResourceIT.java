package es.upm.api.infrastructure.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.upm.api.domain.model.EventType;
import es.upm.api.domain.model.Status;
import es.upm.api.domain.model.UserDto;
import es.upm.api.domain.services.EngagementLetterService;
import es.upm.api.domain.webclients.UserWebClient;
import es.upm.api.infrastructure.dtos.CommentCreateDto;
import es.upm.api.infrastructure.dtos.CommentDto;
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

        // Arrange - Create event
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

        UUID fakeAuthorId = UUID.randomUUID();
        String fakeContent = "Non-existent comment";
        String fakeDate = "2026-04-15T10:30:00";

        mockMvc.perform(delete(EventResource.EVENTS + EventResource.ID_COMMENTS, eventId)
                        .param("authorId", fakeAuthorId.toString())
                        .param("content", fakeContent)
                        .param("createdDate", fakeDate)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }


    @Test
    @WithMockUser(username = "600000001", authorities = {"ROLE_admin"})
    void testDeleteCommentFromNonExistentEventShouldFail() throws Exception {

        // Non-existent eventId
        UUID fakeEventId = UUID.randomUUID();

        UUID fakeAuthorId = UUID.randomUUID();
        String fakeContent = "Some comment";
        String fakeDate = "2026-04-15T10:30:00";

        mockMvc.perform(delete(EventResource.EVENTS + EventResource.ID_COMMENTS, fakeEventId)
                        .param("authorId", fakeAuthorId.toString())
                        .param("content", fakeContent)
                        .param("createdDate", fakeDate)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }


    @Test
    @WithMockUser(username = "600000002", authorities = {"ROLE_admin"})
    void testDeleteCommentAsDifferentUserShouldFail() throws Exception {

        // 1. Crear evento
        EventCreateDto eventCreateDto = EventCreateDto.builder()
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Event")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();

        String eventResponse = mockMvc.perform(post(EventResource.EVENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventCreateDto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String eventId = objectMapper.readTree(eventResponse).get("id").asText();

        // 2. Usuario 1 (autor comentario)
        UUID user1Id = UUID.randomUUID();

        when(userWebClient.readUserByMobile("600000001"))
                .thenReturn(UserDto.builder().id(user1Id).build());

        // 3. Usuario 2 (intenta borrar)
        UUID user2Id = UUID.randomUUID();

        when(userWebClient.readUserByMobile("600000002"))
                .thenReturn(UserDto.builder().id(user2Id).build());

        // 4. Crear comentario como user1
        CommentCreateDto commentCreateDto = CommentCreateDto.builder()
                .content("Comment by user1")
                .build();

        mockMvc.perform(post(EventResource.EVENTS + EventResource.ID_COMMENTS, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentCreateDto))
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("600000001").roles("admin")))
                .andExpect(status().isCreated());

        // 5. Obtener comentario creado
        String eventWithComment = mockMvc.perform(get(EventResource.EVENTS + EventResource.ID_ID, eventId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var commentNode = objectMapper.readTree(eventWithComment)
                .get("comments")
                .get(0);

        String authorId = commentNode.get("authorId").asText();
        String createdDate = commentNode.get("createdDate").asText();
        String content = commentNode.get("content").asText();

        // 6. DELETE como otro usuario → debe fallar
        mockMvc.perform(delete(EventResource.EVENTS + EventResource.ID_COMMENTS, eventId)
                        .param("authorId", authorId)
                        .param("createdDate", createdDate)
                        .param("content", content)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("600000002").roles("admin")))
                .andExpect(status().isForbidden());
    }


    @Test
    @WithMockUser(username = "600000001", authorities = {"ROLE_admin"})
    void testDeleteCommentMultipleCommentsRemovesOnlyOne() throws Exception {

        // 1. Crear evento
        EventCreateDto eventCreateDto = EventCreateDto.builder()
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Event with multiple comments")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();

        String eventResponse = mockMvc.perform(post(EventResource.EVENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventCreateDto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String eventId = objectMapper.readTree(eventResponse).get("id").asText();

        // 2. Mock usuario
        UUID userId = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0001");

        when(userWebClient.readUserByMobile("600000001"))
                .thenReturn(UserDto.builder().id(userId).build());

        // 3. Crear primer comentario
        CommentCreateDto comment1 = CommentCreateDto.builder()
                .content("First comment")
                .build();

        mockMvc.perform(post(EventResource.EVENTS + EventResource.ID_COMMENTS, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(comment1)))
                .andExpect(status().isCreated());

        Thread.sleep(1000); // asegurar timestamps distintos

        // 4. Crear segundo comentario
        CommentCreateDto comment2 = CommentCreateDto.builder()
                .content("Second comment")
                .build();

        mockMvc.perform(post(EventResource.EVENTS + EventResource.ID_COMMENTS, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(comment2)))
                .andExpect(status().isCreated());

        // 5. Leer evento y comprobar que hay 2 comentarios
        String eventWithComments = mockMvc.perform(get(EventResource.EVENTS + EventResource.ID_ID, eventId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var commentsNode = objectMapper.readTree(eventWithComments).get("comments");

        assertThat(commentsNode.size(), is(2));

        // 6. Tomar el primer comentario
        var firstComment = commentsNode.get(0);

        String authorId = firstComment.get("authorId").asText();
        String createdDate = firstComment.get("createdDate").asText();
        String content = firstComment.get("content").asText();

        // 7. DELETE del primer comentario
        mockMvc.perform(delete(EventResource.EVENTS + EventResource.ID_COMMENTS, eventId)
                        .param("authorId", authorId)
                        .param("createdDate", createdDate)
                        .param("content", content))
                .andExpect(status().isNoContent());

        // 8. Leer de nuevo evento
        String eventAfterDelete = mockMvc.perform(get(EventResource.EVENTS + EventResource.ID_ID, eventId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var remainingComments = objectMapper.readTree(eventAfterDelete).get("comments");

        // 9. Solo queda 1 comentario
        assertThat(remainingComments.size(), is(1));
        assertThat(remainingComments.get(0).get("content").asText(), is("Second comment"));
    }


    @Test
    @WithMockUser(username = "600000001", authorities = {"ROLE_manager"})
    void testDeleteCommentWithManagerRole() throws Exception {

        // 1. Crear evento
        EventCreateDto eventCreateDto = EventCreateDto.builder()
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Event")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();

        String eventResponse = mockMvc.perform(post(EventResource.EVENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventCreateDto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String eventId = objectMapper.readTree(eventResponse).get("id").asText();

        // 2. Mock usuario (autor comentario)
        UUID userId = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0001");

        when(userWebClient.readUserByMobile("600000001"))
                .thenReturn(UserDto.builder().id(userId).build());

        // 3. Crear comentario
        CommentCreateDto commentCreateDto = CommentCreateDto.builder()
                .content("Test comment")
                .build();

        mockMvc.perform(post(EventResource.EVENTS + EventResource.ID_COMMENTS, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentCreateDto)))
                .andExpect(status().isCreated());

        // 4. Obtener comentario creado
        String eventWithComment = mockMvc.perform(get(EventResource.EVENTS + EventResource.ID_ID, eventId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var commentNode = objectMapper.readTree(eventWithComment)
                .get("comments")
                .get(0);

        String authorId = commentNode.get("authorId").asText();
        String createdDate = commentNode.get("createdDate").asText();
        String content = commentNode.get("content").asText();

        // 5. DELETE como manager
        mockMvc.perform(delete(EventResource.EVENTS + EventResource.ID_COMMENTS, eventId)
                        .param("authorId", authorId)
                        .param("createdDate", createdDate)
                        .param("content", content))
                .andExpect(status().isNoContent());
    }


    @Test
    void testDeleteCommentWithoutAuthenticationShouldFail() throws Exception {
        // Arrange
        CommentDto deleteDto = CommentDto.builder()
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
        UUID fakeEventId = UUID.randomUUID();
        UUID fakeAuthorId = UUID.randomUUID();
        String fakeContent = "test";
        String fakeDate = "2026-04-15T10:30:00";

        mockMvc.perform(delete(EventResource.EVENTS + EventResource.ID_COMMENTS, fakeEventId)
                        .param("authorId", fakeAuthorId.toString())
                        .param("content", fakeContent)
                        .param("createdDate", fakeDate))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_admin"})
    void testGetTimelineEvents_DefaultDescending() throws Exception {

        UUID engagementId = UUID.randomUUID();

        EventCreateDto event1 = EventCreateDto.builder()
                .eventDate(LocalDateTime.now().plusDays(1))
                .type(EventType.MILESTONE)
                .title("Event 1")
                .status(Status.PENDING)
                .engagementLetterId(engagementId)
                .build();

        EventCreateDto event2 = EventCreateDto.builder()
                .eventDate(LocalDateTime.now().plusDays(3))
                .type(EventType.PHASES)
                .title("Event 2")
                .status(Status.IN_PROGRESS)
                .engagementLetterId(engagementId)
                .build();

        EventCreateDto event3 = EventCreateDto.builder()
                .eventDate(LocalDateTime.now().plusDays(2))
                .type(EventType.STANDARD_EVENT)
                .title("Event 3")
                .status(Status.COMPLETED)
                .engagementLetterId(engagementId)
                .build();

        mockMvc.perform(post(EventResource.EVENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post(EventResource.EVENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event2)))
                .andExpect(status().isCreated());

        mockMvc.perform(post(EventResource.EVENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event3)))
                .andExpect(status().isCreated());

        mockMvc.perform(get(EventResource.EVENTS + "/engagement-letter/" + engagementId + "/timeline-events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].title", is("Event 2")))
                .andExpect(jsonPath("$[1].title", is("Event 3")))
                .andExpect(jsonPath("$[2].title", is("Event 1")));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_admin"})
    void testGetTimelineEvents_AscendingOrder() throws Exception {

        UUID engagementId = UUID.randomUUID();

        EventCreateDto event1 = EventCreateDto.builder()
                .eventDate(LocalDateTime.now().plusDays(1))
                .type(EventType.MILESTONE)
                .title("Event 1")
                .status(Status.PENDING)
                .engagementLetterId(engagementId)
                .build();

        EventCreateDto event2 = EventCreateDto.builder()
                .eventDate(LocalDateTime.now().plusDays(3))
                .type(EventType.PHASES)
                .title("Event 2")
                .status(Status.IN_PROGRESS)
                .engagementLetterId(engagementId)
                .build();

        EventCreateDto event3 = EventCreateDto.builder()
                .eventDate(LocalDateTime.now().plusDays(2))
                .type(EventType.STANDARD_EVENT)
                .title("Event 3")
                .status(Status.COMPLETED)
                .engagementLetterId(engagementId)
                .build();

        mockMvc.perform(post(EventResource.EVENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post(EventResource.EVENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event2)))
                .andExpect(status().isCreated());

        mockMvc.perform(post(EventResource.EVENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event3)))
                .andExpect(status().isCreated());

        mockMvc.perform(get(EventResource.EVENTS + "/engagement-letter/" + engagementId + "/timeline-events?ascending=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].title", is("Event 1")))
                .andExpect(jsonPath("$[1].title", is("Event 3")))
                .andExpect(jsonPath("$[2].title", is("Event 2")));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_admin"})
    void testGetTimelineEvents_FilterByType() throws Exception {

        UUID engagementId = UUID.randomUUID();

        EventCreateDto event1 = EventCreateDto.builder()
                .eventDate(LocalDateTime.now().plusDays(1))
                .type(EventType.MILESTONE)
                .title("Event 1")
                .status(Status.PENDING)
                .engagementLetterId(engagementId)
                .build();

        EventCreateDto event2 = EventCreateDto.builder()
                .eventDate(LocalDateTime.now().plusDays(2))
                .type(EventType.PHASES)
                .title("Event 2")
                .status(Status.IN_PROGRESS)
                .engagementLetterId(engagementId)
                .build();

        mockMvc.perform(post(EventResource.EVENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post(EventResource.EVENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event2)))
                .andExpect(status().isCreated());

        mockMvc.perform(get(EventResource.EVENTS + "/engagement-letter/" + engagementId + "/timeline-events?type=MILESTONE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Event 1")));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_admin"})
    void testGetTimelineEvents_FilterByStatus() throws Exception {

        UUID engagementId = UUID.randomUUID();

        EventCreateDto event1 = EventCreateDto.builder()
                .eventDate(LocalDateTime.now().plusDays(1))
                .type(EventType.MILESTONE)
                .title("Event 1")
                .status(Status.PENDING)
                .engagementLetterId(engagementId)
                .build();

        EventCreateDto event2 = EventCreateDto.builder()
                .eventDate(LocalDateTime.now().plusDays(2))
                .type(EventType.PHASES)
                .title("Event 2")
                .status(Status.COMPLETED)
                .engagementLetterId(engagementId)
                .build();

        mockMvc.perform(post(EventResource.EVENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post(EventResource.EVENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event2)))
                .andExpect(status().isCreated());

        mockMvc.perform(get(EventResource.EVENTS + "/engagement-letter/" + engagementId + "/timeline-events?status=PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Event 1")));
    }


    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_admin"})
    void testGetTimelineEvents_FilterByTypeAndStatus() throws Exception {

        UUID engagementId = UUID.randomUUID();

        EventCreateDto event1 = EventCreateDto.builder()
                .eventDate(LocalDateTime.now().plusDays(1))
                .type(EventType.MILESTONE)
                .status(Status.PENDING)
                .title("Keep")
                .engagementLetterId(engagementId)
                .build();

        EventCreateDto event2 = EventCreateDto.builder()
                .eventDate(LocalDateTime.now().plusDays(2))
                .type(EventType.MILESTONE)
                .status(Status.COMPLETED)
                .title("Remove")
                .engagementLetterId(engagementId)
                .build();

        mockMvc.perform(post(EventResource.EVENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post(EventResource.EVENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event2)))
                .andExpect(status().isCreated());

        mockMvc.perform(get(EventResource.EVENTS +
                        "/engagement-letter/" + engagementId +
                        "/timeline-events?type=MILESTONE&status=PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Keep")));
    }


}






