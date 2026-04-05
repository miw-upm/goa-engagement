package es.upm.api.domain.services;

import es.upm.api.domain.model.Comment;
import es.upm.api.domain.model.Event;
import es.upm.api.domain.model.EventType;
import es.upm.api.domain.model.Status;
import es.upm.api.domain.persistence.EventPersistence;
import es.upm.api.infrastructure.mongodb.persistence.EventPersistenceMongodb;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DataMongoTest
@ActiveProfiles("test")
@Import({EventService.class, EventPersistenceMongodb.class})
class EventServiceIT {

    @TestConfiguration
    static class EventServiceTestConfiguration {
        @Bean
        public EventService eventService(EventPersistence eventPersistence, EngagementLetterService engagementLetterService) {
            return new EventService(eventPersistence, engagementLetterService);
        }

        @Bean
        public EngagementLetterService engagementLetterService() {
            return Mockito.mock(EngagementLetterService.class);
        }
    }

    @Autowired
    private EventService eventService;

    @Autowired
    private EngagementLetterService engagementLetterService;

    private UUID engagementLetterId;
    private LocalDateTime eventDate;

    @BeforeEach
    void setUp() {
        engagementLetterId = UUID.randomUUID();
        eventDate = LocalDateTime.now().plusDays(1);
        Mockito.reset(engagementLetterService);
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
    void testCreateEventWithComments() {
        // Arrange
        List<Comment> comments = new ArrayList<>();
        comments.add(Comment.builder()
                .createdDate(LocalDateTime.now())
                .content("First comment")
                .build());
        comments.add(Comment.builder()
                .createdDate(LocalDateTime.now())
                .content("Second comment")
                .build());

        Event event = Event.builder()
                .eventDate(eventDate)
                .type(EventType.PHASES)
                .title("Event with Comments")
                .description("Test Description")
                .status(Status.IN_PROGRESS)
                .engagementLetterId(engagementLetterId)
                .comments(comments)
                .build();

        // Act
        Event createdEvent = eventService.create(event);

        // Assert
        assertThat(createdEvent).isNotNull();
        assertThat(createdEvent.getId()).isNotNull();
        assertThat(createdEvent.getCreatedDate()).isNotNull();
        assertThat(createdEvent.getComments()).hasSize(2);
        assertThat(createdEvent.getComments().get(0).getContent()).isEqualTo("First comment");
        assertThat(createdEvent.getComments().get(1).getContent()).isEqualTo("Second comment");
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
}

