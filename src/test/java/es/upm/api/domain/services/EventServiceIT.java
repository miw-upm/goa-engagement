package es.upm.api.domain.services;

import es.upm.api.domain.model.*;
import es.upm.api.domain.persistence.EventPersistence;
import es.upm.api.domain.webclients.UserWebClient;
import es.upm.api.infrastructure.dtos.EventUpdateDto;
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

    @Test
    void testUpdateEventAllFields() {
        // Arrange
        Event event = Event.builder()
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Original title")
                .description("Original description")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();
        Event createdEvent = eventService.create(event);
        UUID eventId = createdEvent.getId();

        EventUpdateDto updateDto = EventUpdateDto.builder()
                .eventDate(eventDate.plusDays(1))
                .type(EventType.PHASES)
                .title("Updated title")
                .description("Updated description")
                .status(Status.IN_PROGRESS)
                .build();

        // Act
        Event updatedEvent = eventService.update(eventId, updateDto);

        // Assert
        assertThat(updatedEvent.getId()).isEqualTo(eventId);
        assertThat(updatedEvent.getType()).isEqualTo(EventType.PHASES);
        assertThat(updatedEvent.getTitle()).isEqualTo("Updated title");
        assertThat(updatedEvent.getDescription()).isEqualTo("Updated description");
        assertThat(updatedEvent.getStatus()).isEqualTo(Status.IN_PROGRESS);
        assertThat(updatedEvent.getEventDate()).isEqualTo(eventDate.plusDays(1));
    }

    @Test
    void testUpdateEventPartial() {
        // Arrange
        Event event = Event.builder()
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Original title")
                .description("Original description")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();
        Event createdEvent = eventService.create(event);
        UUID eventId = createdEvent.getId();

        // Update only title
        EventUpdateDto updateDto = EventUpdateDto.builder()
                .title("Updated title only")
                .build();

        // Act
        Event updatedEvent = eventService.update(eventId, updateDto);

        // Assert - Only title should change
        assertThat(updatedEvent.getTitle()).isEqualTo("Updated title only");
        assertThat(updatedEvent.getDescription()).isEqualTo("Original description");
        assertThat(updatedEvent.getType()).isEqualTo(EventType.MILESTONE);
        assertThat(updatedEvent.getStatus()).isEqualTo(Status.PENDING);
    }

    @Test
    void testUpdateEventPreservesId() {
        // Arrange
        Event event = Event.builder()
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Event")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();
        Event createdEvent = eventService.create(event);
        UUID originalId = createdEvent.getId();

        EventUpdateDto updateDto = EventUpdateDto.builder()
                .title("Updated")
                .build();

        // Act
        Event updatedEvent = eventService.update(originalId, updateDto);

        // Assert - ID should not change
        assertThat(updatedEvent.getId()).isEqualTo(originalId);
    }

    @Test
    void testUpdateEventPreservesEngagementLetterId() {
        // Arrange
        Event event = Event.builder()
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Original title")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();
        Event createdEvent = eventService.create(event);
        UUID eventId = createdEvent.getId();

        EventUpdateDto updateDto = EventUpdateDto.builder()
                .title("Updated title")
                .build();

        // Act
        Event updatedEvent = eventService.update(eventId, updateDto);

        // Assert - engagementLetterId should not change
        assertThat(updatedEvent.getEngagementLetterId()).isEqualTo(engagementLetterId);
    }

    @Test
    void testUpdateEventPreservesComments() {
        // Arrange
        Event event = Event.builder()
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Event with comments")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .comments(new ArrayList<>())
                .build();
        Event createdEvent = eventService.create(event);
        UUID eventId = createdEvent.getId();

        // Add comments
        UserDto authenticatedUser = UserDto.builder()
                .id(UUID.randomUUID())
                .mobile("600000001")
                .firstName("Laura")
                .build();
        Mockito.when(userWebClient.readUserByMobile(authenticatedUser.getMobile()))
                .thenReturn(authenticatedUser);
        eventService.addComment(eventId, authenticatedUser.getMobile(), "Comment 1");
        eventService.addComment(eventId, authenticatedUser.getMobile(), "Comment 2");

        EventUpdateDto updateDto = EventUpdateDto.builder()
                .title("Updated title")
                .build();

        // Act
        Event updatedEvent = eventService.update(eventId, updateDto);

        // Assert - Comments should be preserved
        assertThat(updatedEvent.getComments()).hasSize(2);
    }

    @Test
    void testUpdateNonExistentEvent_ShouldFail() {
        // Arrange
        EventUpdateDto updateDto = EventUpdateDto.builder()
                .title("Updated")
                .build();

        // Act & Assert
        assertThatThrownBy(() -> eventService.update(UUID.randomUUID(), updateDto))
                .hasMessageContaining("The Event ID doesn't exist");
    }

    @Test
    void testUpdateEventWithEmptyDto() {
        // Arrange
        Event event = Event.builder()
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Original title")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();
        Event createdEvent = eventService.create(event);
        UUID eventId = createdEvent.getId();

        EventUpdateDto updateDto = EventUpdateDto.builder().build();

        // Act
        Event updatedEvent = eventService.update(eventId, updateDto);

        // Assert - Nothing should change
        assertThat(updatedEvent.getTitle()).isEqualTo("Original title");
        assertThat(updatedEvent.getType()).isEqualTo(EventType.MILESTONE);
    }

    @Test
    void testReadEventById() {
        // Arrange
        Event event = Event.builder()
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Event to read")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();
        Event createdEvent = eventService.create(event);
        UUID eventId = createdEvent.getId();

        // Act
        Event retrievedEvent = eventService.readById(eventId);

        // Assert
        assertThat(retrievedEvent).isNotNull();
        assertThat(retrievedEvent.getId()).isEqualTo(eventId);
        assertThat(retrievedEvent.getTitle()).isEqualTo("Event to read");
        assertThat(retrievedEvent.getType()).isEqualTo(EventType.MILESTONE);
    }

    @Test
    void testReadNonExistentEvent_ShouldFail() {
        // Act & Assert
        assertThatThrownBy(() -> eventService.readById(UUID.randomUUID()))
                .hasMessageContaining("The Event ID doesn't exist");
    }

    @Test
    void testFindEventsByEngagementLetterId() {
        // Arrange
        Event event1 = Event.builder()
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Event 1")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();
        Event event2 = Event.builder()
                .eventDate(eventDate.plusDays(1))
                .type(EventType.PHASES)
                .title("Event 2")
                .status(Status.IN_PROGRESS)
                .engagementLetterId(engagementLetterId)
                .build();

        eventService.create(event1);
        eventService.create(event2);

        // Act
        var events = eventService.findByEngagementLetterId(engagementLetterId).toList();

        // Assert
        assertThat(events).hasSize(2);
        assertThat(events.get(0).getTitle()).isEqualTo("Event 1");
        assertThat(events.get(1).getTitle()).isEqualTo("Event 2");
    }

    @Test
    void testFindEventsByEngagementLetterId_OrderedByDate() {
        // Arrange
        UUID randomEngagementLetterId = UUID.randomUUID();
        Mockito.when(engagementLetterService.readById(any(UUID.class)))
                .thenReturn(new EngagementLetter());

        // Create events with different dates
        Event eventLater = Event.builder()
                .eventDate(eventDate.plusDays(3))
                .type(EventType.PHASES)
                .title("Event Later")
                .status(Status.IN_PROGRESS)
                .engagementLetterId(randomEngagementLetterId)
                .build();
        Event eventEarlier = Event.builder()
                .eventDate(eventDate.plusDays(1))
                .type(EventType.MILESTONE)
                .title("Event Earlier")
                .status(Status.PENDING)
                .engagementLetterId(randomEngagementLetterId)
                .build();

        eventService.create(eventLater);
        eventService.create(eventEarlier);

        // Act
        var events = eventService.findByEngagementLetterId(randomEngagementLetterId).toList();

        // Assert - Should be ordered by eventDate ASC
        assertThat(events).hasSize(2);
        assertThat(events.get(0).getTitle()).isEqualTo("Event Earlier");
        assertThat(events.get(1).getTitle()).isEqualTo("Event Later");
    }

    @Test
    void testFindEventsByEngagementLetterId_Empty() {
        // Act
        var events = eventService.findByEngagementLetterId(UUID.randomUUID()).toList();

        // Assert
        assertThat(events).isEmpty();
    }
}

