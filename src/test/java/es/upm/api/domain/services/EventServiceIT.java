package es.upm.api.domain.services;

import es.upm.api.domain.model.*;
import es.upm.api.domain.persistence.EventPersistence;
import es.upm.api.domain.webclients.UserWebClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
@ActiveProfiles("test")
class EventServiceIT {

    @Autowired
    private EventService eventService;

    @Autowired
    private EventPersistence eventPersistence;

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
        Mockito.reset(engagementLetterService);
        Mockito.reset(userWebClient);
        Mockito.when(engagementLetterService.readById(any(UUID.class)))
                .thenReturn(new EngagementLetter());
    }

    @Test
    void testCreateEventWithoutComments() {
        // Arrange
        Event event = Event.builder()
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Test Event")
                .description("Test Description")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .comments(new ArrayList<>())
                .build();

        // Act
        Event createdEvent = eventService.create(event);

        // Assert
        assertThat(createdEvent).isNotNull();
        assertThat(createdEvent.getId()).isNotNull();
        assertThat(createdEvent.getCreatedDate()).isNotNull();
        assertThat(createdEvent.getTitle()).isEqualTo("Test Event");
        assertThat(createdEvent.getDescription()).isEqualTo("Test Description");
        assertThat(createdEvent.getType()).isEqualTo(EventType.MILESTONE);
        assertThat(createdEvent.getStatus()).isEqualTo(Status.PENDING);
        assertThat(createdEvent.getEngagementLetterId()).isEqualTo(engagementLetterId);
        assertThat(createdEvent.getComments()).isEmpty();
    }

    @Test
    void testCreateEventStartsWithoutComments() {
        Event event = Event.builder()
                .eventDate(eventDate)
                .type(EventType.PHASES)
                .title("Event without initial comments")
                .description("Test Description")
                .status(Status.IN_PROGRESS)
                .engagementLetterId(engagementLetterId)
                .build();

        Event createdEvent = eventService.create(event);

        assertThat(createdEvent).isNotNull();
        assertThat(createdEvent.getId()).isNotNull();
        assertThat(createdEvent.getCreatedDate()).isNotNull();
        assertThat(createdEvent.getComments()).isEmpty();
    }

    @Test
    void testCreateEventGeneratesUUID() {
        // Arrange
        Event event = Event.builder()
                .eventDate(eventDate)
                .type(EventType.STANDARD_EVENT)
                .title("Event for UUID test")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();

        assertThat(event.getId()).isNull();

        // Act
        Event createdEvent = eventService.create(event);

        // Assert
        assertThat(createdEvent.getId()).isNotNull();
        assertThat(createdEvent.getId()).isInstanceOf(UUID.class);
    }

    @Test
    void testCreateEventGeneratesCreatedDate() {
        // Arrange
        Event event = Event.builder()
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Event for createdDate test")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();

        assertThat(event.getCreatedDate()).isNull();

        LocalDateTime beforeCreation = LocalDateTime.now();

        // Act
        Event createdEvent = eventService.create(event);

        LocalDateTime afterCreation = LocalDateTime.now();

        // Assert
        assertThat(createdEvent.getCreatedDate()).isNotNull();
        assertThat(createdEvent.getCreatedDate()).isAfterOrEqualTo(beforeCreation);
        assertThat(createdEvent.getCreatedDate()).isBeforeOrEqualTo(afterCreation);
    }

    @Test
    void testCreateEventCallsEngagementLetterService() {
        // Arrange
        Event event = Event.builder()
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Event for service call test")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();

        // Act
        eventService.create(event);

        // Assert
        Mockito.verify(engagementLetterService, Mockito.times(1)).readById(engagementLetterId);
    }

    @Test
    void testCreateEventWithAllEventTypes() {
        // Test MILESTONE
        Event milestoneEvent = Event.builder()
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Milestone Event")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();

        Event createdMilestone = eventService.create(milestoneEvent);
        assertThat(createdMilestone.getType()).isEqualTo(EventType.MILESTONE);

        // Test PHASES
        Event phasesEvent = Event.builder()
                .eventDate(eventDate)
                .type(EventType.PHASES)
                .title("Phases Event")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();

        Event createdPhases = eventService.create(phasesEvent);
        assertThat(createdPhases.getType()).isEqualTo(EventType.PHASES);

        // Test STANDARD_EVENT
        Event standardEvent = Event.builder()
                .eventDate(eventDate)
                .type(EventType.STANDARD_EVENT)
                .title("Standard Event")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();

        Event createdStandard = eventService.create(standardEvent);
        assertThat(createdStandard.getType()).isEqualTo(EventType.STANDARD_EVENT);
    }

    @Test
    void testCreateEventWithAllStatus() {
        // Test PENDING
        Event pendingEvent = Event.builder()
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Pending Event")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();

        Event createdPending = eventService.create(pendingEvent);
        assertThat(createdPending.getStatus()).isEqualTo(Status.PENDING);

        // Test IN_PROGRESS
        Event inProgressEvent = Event.builder()
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("In Progress Event")
                .status(Status.IN_PROGRESS)
                .engagementLetterId(engagementLetterId)
                .build();

        Event createdInProgress = eventService.create(inProgressEvent);
        assertThat(createdInProgress.getStatus()).isEqualTo(Status.IN_PROGRESS);

        // Test COMPLETED
        Event completedEvent = Event.builder()
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Completed Event")
                .status(Status.COMPLETED)
                .engagementLetterId(engagementLetterId)
                .build();

        Event createdCompleted = eventService.create(completedEvent);
        assertThat(createdCompleted.getStatus()).isEqualTo(Status.COMPLETED);

        // Test CANCELLED
        Event cancelledEvent = Event.builder()
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Cancelled Event")
                .status(Status.CANCELLED)
                .engagementLetterId(engagementLetterId)
                .build();

        Event createdCancelled = eventService.create(cancelledEvent);
        assertThat(createdCancelled.getStatus()).isEqualTo(Status.CANCELLED);
    }

    @Test
    void testCreateEventPreservesEventDate() {
        // Arrange
        LocalDateTime specificEventDate = LocalDateTime.of(2025, 12, 31, 23, 59, 59);
        Event event = Event.builder()
                .eventDate(specificEventDate)
                .type(EventType.MILESTONE)
                .title("Event with specific date")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();

        // Act
        Event createdEvent = eventService.create(event);

        // Assert
        assertThat(createdEvent.getEventDate()).isEqualTo(specificEventDate);
    }

    @Test
    void testCreateEventWithNullCommentsList() {
        // Arrange
        Event event = Event.builder()
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Event with null comments")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .comments(null)
                .build();

        // Act
        Event createdEvent = eventService.create(event);

        // Assert
        assertThat(createdEvent).isNotNull();
        assertThat(createdEvent.getId()).isNotNull();
    }

    @Test
    void testCreateEventWithoutDescription() {
        // Arrange
        Event event = Event.builder()
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Event without description")
                .description(null)
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();

        // Act
        Event createdEvent = eventService.create(event);

        // Assert
        assertThat(createdEvent).isNotNull();
        assertThat(createdEvent.getDescription()).isNull();
    }

    @Test
    void testAddCommentToEvent() {
        Event event = Event.builder()
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Event with comment")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .comments(new ArrayList<>())
                .build();
        Event createdEvent = eventService.create(event);
        UserDto authenticatedUser = UserDto.builder()
                .id(UUID.randomUUID())
                .mobile("600000001")
                .firstName("Laura")
                .build();
        Mockito.when(userWebClient.readUserByMobile(authenticatedUser.getMobile()))
                .thenReturn(authenticatedUser);

        Comment createdComment = eventService.addComment(
                createdEvent.getId(),
                authenticatedUser.getMobile(),
                "Seguimiento del evento"
        );

        Event persistedEvent = eventPersistence.readById(createdEvent.getId());
        assertThat(createdComment.getCreatedDate()).isNotNull();
        assertThat(createdComment.getContent()).isEqualTo("Seguimiento del evento");
        assertThat(createdComment.getAuthorId()).isEqualTo(authenticatedUser.getId());
        assertThat(persistedEvent.getComments()).hasSize(1);
        assertThat(persistedEvent.getComments().getFirst().getContent()).isEqualTo("Seguimiento del evento");
        assertThat(persistedEvent.getComments().getFirst().getAuthorId()).isEqualTo(authenticatedUser.getId());
    }

    @Test
    void testAddCommentToMissingEvent() {
        UserDto authenticatedUser = UserDto.builder()
                .id(UUID.randomUUID())
                .mobile("600000001")
                .build();
        Mockito.when(userWebClient.readUserByMobile(authenticatedUser.getMobile()))
                .thenReturn(authenticatedUser);

        assertThatThrownBy(() -> eventService.addComment(
                UUID.randomUUID(),
                authenticatedUser.getMobile(),
                "Comentario"
        )).hasMessageContaining("The Event ID doesn't exist");
    }
}

