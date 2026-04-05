package es.upm.api.infrastructure.resources;

import es.upm.api.domain.model.EventType;
import es.upm.api.domain.model.Status;
import es.upm.api.domain.services.EngagementLetterService;
import es.upm.api.infrastructure.dtos.CommentCreateDto;
import es.upm.api.infrastructure.dtos.EventCreateDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    private UUID engagementLetterId;
    private LocalDateTime eventDate;

    @BeforeEach
    void setUp() {
        engagementLetterId = UUID.randomUUID();
        eventDate = LocalDateTime.now().plusDays(1);
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
                .comments(null)
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
    void testCreateEventWithComments() throws Exception {
        // Arrange
        List<CommentCreateDto> comments = new ArrayList<>();
        comments.add(CommentCreateDto.builder().content("First comment").build());
        comments.add(CommentCreateDto.builder().content("Second comment").build());

        EventCreateDto eventCreateDto = EventCreateDto.builder()
                .eventDate(eventDate)
                .type(EventType.PHASES)
                .title("Event with Comments")
                .description("Event Description")
                .status(Status.IN_PROGRESS)
                .engagementLetterId(engagementLetterId)
                .comments(comments)
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
                .andExpect(jsonPath("$.title", is("Event with Comments")))
                .andExpect(jsonPath("$.status", is("IN_PROGRESS")))
                .andExpect(jsonPath("$.comments", hasSize(2)))
                .andExpect(jsonPath("$.comments[0].content", is("First comment")))
                .andExpect(jsonPath("$.comments[0].createdDate", notNullValue()))
                .andExpect(jsonPath("$.comments[1].content", is("Second comment")))
                .andExpect(jsonPath("$.comments[1].createdDate", notNullValue()));
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
    void testCreateEventWithEmptyCommentContent_ShouldFail() throws Exception {
        // Arrange
        List<CommentCreateDto> comments = new ArrayList<>();
        comments.add(CommentCreateDto.builder().content("  ").build()); // Empty content

        EventCreateDto eventCreateDto = EventCreateDto.builder()
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Event Title")
                .description("Event Description")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .comments(comments)
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
}






