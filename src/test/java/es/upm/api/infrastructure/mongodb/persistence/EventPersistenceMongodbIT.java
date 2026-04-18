package es.upm.api.infrastructure.mongodb.persistence;

import es.upm.api.domain.model.Comment;
import es.upm.api.domain.model.Event;
import es.upm.api.domain.model.EventType;
import es.upm.api.domain.model.Status;
import es.upm.api.infrastructure.mongodb.entities.CommentEntity;
import es.upm.api.infrastructure.mongodb.entities.EventEntity;
import es.upm.api.infrastructure.mongodb.repositories.EventRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DataMongoTest
@ActiveProfiles("test")
class EventPersistenceMongodbIT {

    @Autowired
    private EventRepository eventRepository;

    private EventPersistenceMongodb eventPersistence;
    private UUID eventId;
    private UUID engagementLetterId;
    private LocalDateTime eventDate;
    private LocalDateTime createdDate;
    private UUID authorId;

    @BeforeEach
    void setUp() {
        eventPersistence = new EventPersistenceMongodb(eventRepository);
        eventRepository.deleteAll();

        eventId = UUID.randomUUID();
        engagementLetterId = UUID.randomUUID();
        eventDate = LocalDateTime.now().plusDays(1);
        createdDate = LocalDateTime.now();
        authorId = UUID.randomUUID();
    }

    @Test
    void testCreateEventWithoutComments() {
        // Arrange
        Event event = Event.builder()
                .id(eventId)
                .createdDate(createdDate)
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Test Event")
                .description("Test Description")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .comments(new ArrayList<>())
                .build();

        // Act
        eventPersistence.create(event);

        // Assert
        Optional<EventEntity> savedEvent = eventRepository.findById(eventId);
        assertThat(savedEvent).isPresent();
        assertThat(savedEvent.get().getId()).isEqualTo(eventId);
        assertThat(savedEvent.get().getTitle()).isEqualTo("Test Event");
        assertThat(savedEvent.get().getDescription()).isEqualTo("Test Description");
        assertThat(savedEvent.get().getType()).isEqualTo(EventType.MILESTONE);
        assertThat(savedEvent.get().getStatus()).isEqualTo(Status.PENDING);
        assertThat(savedEvent.get().getEngagementLetterId()).isEqualTo(engagementLetterId);
        assertThat(savedEvent.get().getComments()).isEmpty();
    }

    @Test
    void testCreateEventStartsWithoutComments() {
        Event event = Event.builder()
                .id(eventId)
                .createdDate(createdDate)
                .eventDate(eventDate)
                .type(EventType.PHASES)
                .title("Event without Comments")
                .description("Test Description")
                .status(Status.IN_PROGRESS)
                .engagementLetterId(engagementLetterId)
                .build();

        eventPersistence.create(event);

        Optional<EventEntity> savedEvent = eventRepository.findById(eventId);
        assertThat(savedEvent).isPresent();
        assertThat(savedEvent.get().getComments()).isEmpty();
    }

    @Test
    void testCreateEventWithAllEventTypes() {
        // Test MILESTONE
        Event milestoneEvent = Event.builder()
                .id(UUID.randomUUID())
                .createdDate(createdDate)
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Milestone Event")
                .description("Test")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();

        eventPersistence.create(milestoneEvent);

        Optional<EventEntity> savedEvent = eventRepository.findById(milestoneEvent.getId());
        assertThat(savedEvent).isPresent();
        assertThat(savedEvent.get().getType()).isEqualTo(EventType.MILESTONE);

        // Test PHASES
        Event phasesEvent = Event.builder()
                .id(UUID.randomUUID())
                .createdDate(createdDate)
                .eventDate(eventDate)
                .type(EventType.PHASES)
                .title("Phases Event")
                .description("Test")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();

        eventPersistence.create(phasesEvent);

        savedEvent = eventRepository.findById(phasesEvent.getId());
        assertThat(savedEvent).isPresent();
        assertThat(savedEvent.get().getType()).isEqualTo(EventType.PHASES);

        // Test STANDARD_EVENT
        Event standardEvent = Event.builder()
                .id(UUID.randomUUID())
                .createdDate(createdDate)
                .eventDate(eventDate)
                .type(EventType.STANDARD_EVENT)
                .title("Standard Event")
                .description("Test")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();

        eventPersistence.create(standardEvent);

        savedEvent = eventRepository.findById(standardEvent.getId());
        assertThat(savedEvent).isPresent();
        assertThat(savedEvent.get().getType()).isEqualTo(EventType.STANDARD_EVENT);
    }

    @Test
    void testCreateEventWithAllStatus() {
        // Test PENDING
        Event pendingEvent = Event.builder()
                .id(UUID.randomUUID())
                .createdDate(createdDate)
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Pending Event")
                .description("Test")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();

        eventPersistence.create(pendingEvent);

        Optional<EventEntity> savedEvent = eventRepository.findById(pendingEvent.getId());
        assertThat(savedEvent).isPresent();
        assertThat(savedEvent.get().getStatus()).isEqualTo(Status.PENDING);

        // Test IN_PROGRESS
        Event inProgressEvent = Event.builder()
                .id(UUID.randomUUID())
                .createdDate(createdDate)
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("In Progress Event")
                .description("Test")
                .status(Status.IN_PROGRESS)
                .engagementLetterId(engagementLetterId)
                .build();

        eventPersistence.create(inProgressEvent);

        savedEvent = eventRepository.findById(inProgressEvent.getId());
        assertThat(savedEvent).isPresent();
        assertThat(savedEvent.get().getStatus()).isEqualTo(Status.IN_PROGRESS);

        // Test COMPLETED
        Event completedEvent = Event.builder()
                .id(UUID.randomUUID())
                .createdDate(createdDate)
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Completed Event")
                .description("Test")
                .status(Status.COMPLETED)
                .engagementLetterId(engagementLetterId)
                .build();

        eventPersistence.create(completedEvent);

        savedEvent = eventRepository.findById(completedEvent.getId());
        assertThat(savedEvent).isPresent();
        assertThat(savedEvent.get().getStatus()).isEqualTo(Status.COMPLETED);

        // Test CANCELLED
        Event cancelledEvent = Event.builder()
                .id(UUID.randomUUID())
                .createdDate(createdDate)
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Cancelled Event")
                .description("Test")
                .status(Status.CANCELLED)
                .engagementLetterId(engagementLetterId)
                .build();

        eventPersistence.create(cancelledEvent);

        savedEvent = eventRepository.findById(cancelledEvent.getId());
        assertThat(savedEvent).isPresent();
        assertThat(savedEvent.get().getStatus()).isEqualTo(Status.CANCELLED);
    }

    @Test
    void testCreateEventWithoutDescription() {
        // Arrange
        Event event = Event.builder()
                .id(eventId)
                .createdDate(createdDate)
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Event Without Description")
                .description(null)
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();

        // Act
        eventPersistence.create(event);

        // Assert
        Optional<EventEntity> savedEvent = eventRepository.findById(eventId);
        assertThat(savedEvent).isPresent();
        assertThat(savedEvent.get().getDescription()).isNull();
    }

    @Test
    void testEventEntityToEvent_Conversion() {
        // Arrange
        EventEntity eventEntity = EventEntity.builder()
                .id(eventId)
                .createdDate(createdDate)
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Test Event")
                .description("Test Description")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .comments(new ArrayList<>())
                .build();

        // Act
        Event event = eventEntity.toEvent();

        // Assert
        assertThat(event).isNotNull();
        assertThat(event.getId()).isEqualTo(eventId);
        assertThat(event.getTitle()).isEqualTo("Test Event");
        assertThat(event.getDescription()).isEqualTo("Test Description");
        assertThat(event.getType()).isEqualTo(EventType.MILESTONE);
        assertThat(event.getStatus()).isEqualTo(Status.PENDING);
        assertThat(event.getEngagementLetterId()).isEqualTo(engagementLetterId);
    }

    @Test
    void testEventEntityToEvent_WithComments() {
        // Arrange
        List<CommentEntity> commentEntities = new ArrayList<>();
        commentEntities.add(CommentEntity.builder()
                .authorId(authorId)
                .createdDate(LocalDateTime.now())
                .content("Test comment 1")
                .build());
        commentEntities.add(CommentEntity.builder()
                .authorId(authorId)
                .createdDate(LocalDateTime.now())
                .content("Test comment 2")
                .build());

        EventEntity eventEntity = EventEntity.builder()
                .id(eventId)
                .createdDate(createdDate)
                .eventDate(eventDate)
                .type(EventType.PHASES)
                .title("Event with Comments")
                .description("Test")
                .status(Status.IN_PROGRESS)
                .engagementLetterId(engagementLetterId)
                .comments(commentEntities)
                .build();

        // Act
        Event event = eventEntity.toEvent();

        // Assert
        assertThat(event.getComments()).hasSize(2);
        assertThat(event.getComments().get(0).getAuthorId()).isEqualTo(authorId);
        assertThat(event.getComments().get(0).getContent()).isEqualTo("Test comment 1");
        assertThat(event.getComments().get(1).getAuthorId()).isEqualTo(authorId);
        assertThat(event.getComments().get(1).getContent()).isEqualTo("Test comment 2");
    }

    @Test
    void testEventToEventEntity_Conversion() {
        // Arrange
        Event event = Event.builder()
                .id(eventId)
                .createdDate(createdDate)
                .eventDate(eventDate)
                .type(EventType.STANDARD_EVENT)
                .title("Test Event")
                .description("Test Description")
                .status(Status.COMPLETED)
                .engagementLetterId(engagementLetterId)
                .comments(new ArrayList<>())
                .build();

        // Act
        EventEntity eventEntity = new EventEntity(event);

        // Assert
        assertThat(eventEntity).isNotNull();
        assertThat(eventEntity.getId()).isEqualTo(eventId);
        assertThat(eventEntity.getTitle()).isEqualTo("Test Event");
        assertThat(eventEntity.getDescription()).isEqualTo("Test Description");
        assertThat(eventEntity.getType()).isEqualTo(EventType.STANDARD_EVENT);
        assertThat(eventEntity.getStatus()).isEqualTo(Status.COMPLETED);
        assertThat(eventEntity.getEngagementLetterId()).isEqualTo(engagementLetterId);
    }

    @Test
    void testEventToEventEntity_WithComments() {
        // Arrange
        List<Comment> comments = new ArrayList<>();
        comments.add(Comment.builder()
                .authorId(authorId)
                .createdDate(LocalDateTime.now())
                .content("Comment 1")
                .build());
        comments.add(Comment.builder()
                .authorId(authorId)
                .createdDate(LocalDateTime.now())
                .content("Comment 2")
                .build());

        Event event = Event.builder()
                .id(eventId)
                .createdDate(createdDate)
                .eventDate(eventDate)
                .type(EventType.PHASES)
                .title("Event with Comments")
                .description("Test")
                .status(Status.IN_PROGRESS)
                .engagementLetterId(engagementLetterId)
                .comments(comments)
                .build();

        // Act
        EventEntity eventEntity = new EventEntity(event);

        // Assert
        assertThat(eventEntity.getComments()).hasSize(2);
        assertThat(eventEntity.getComments().get(0).getContent()).isEqualTo("Comment 1");
        assertThat(eventEntity.getComments().get(1).getContent()).isEqualTo("Comment 2");
    }

    @Test
    void testCommentEntityToComment_Conversion() {
        // Arrange
        LocalDateTime commentDate = LocalDateTime.now();
        CommentEntity commentEntity = CommentEntity.builder()
                .authorId(authorId)
                .createdDate(commentDate)
                .content("Test comment")
                .build();

        // Act
        Comment comment = commentEntity.toComment();

        // Assert
        assertThat(comment).isNotNull();
        assertThat(comment.getAuthorId()).isEqualTo(authorId);
        assertThat(comment.getCreatedDate()).isEqualTo(commentDate);
        assertThat(comment.getContent()).isEqualTo("Test comment");
    }

    @Test
    void testCommentToCommentEntity_Conversion() {
        // Arrange
        LocalDateTime commentDate = LocalDateTime.now();
        Comment comment = Comment.builder()
                .authorId(authorId)
                .createdDate(commentDate)
                .content("Test comment")
                .build();

        // Act
        CommentEntity commentEntity = new CommentEntity(comment);

        // Assert
        assertThat(commentEntity).isNotNull();
        assertThat(commentEntity.getAuthorId()).isEqualTo(authorId);
        assertThat(commentEntity.getCreatedDate()).isEqualTo(commentDate);
        assertThat(commentEntity.getContent()).isEqualTo("Test comment");
    }

    @Test
    void testCreateMultipleEvents() {
        // Arrange
        UUID eventId1 = UUID.randomUUID();
        UUID eventId2 = UUID.randomUUID();
        UUID eventId3 = UUID.randomUUID();

        Event event1 = Event.builder()
                .id(eventId1)
                .createdDate(createdDate)
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Event 1")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();

        Event event2 = Event.builder()
                .id(eventId2)
                .createdDate(createdDate)
                .eventDate(eventDate)
                .type(EventType.PHASES)
                .title("Event 2")
                .status(Status.IN_PROGRESS)
                .engagementLetterId(engagementLetterId)
                .build();

        Event event3 = Event.builder()
                .id(eventId3)
                .createdDate(createdDate)
                .eventDate(eventDate)
                .type(EventType.STANDARD_EVENT)
                .title("Event 3")
                .status(Status.COMPLETED)
                .engagementLetterId(engagementLetterId)
                .build();

        // Act
        eventPersistence.create(event1);
        eventPersistence.create(event2);
        eventPersistence.create(event3);

        // Assert
        assertThat(eventRepository.findById(eventId1)).isPresent();
        assertThat(eventRepository.findById(eventId2)).isPresent();
        assertThat(eventRepository.findById(eventId3)).isPresent();
        assertThat(eventRepository.count()).isEqualTo(3);
    }

    @Test
    void testCreateEventWithNullCommentsList() {
        // Arrange
        Event event = Event.builder()
                .id(eventId)
                .createdDate(createdDate)
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Event with null comments")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .comments(null)
                .build();

        // Act
        eventPersistence.create(event);

        // Assert
        Optional<EventEntity> savedEvent = eventRepository.findById(eventId);
        assertThat(savedEvent).isPresent();
        assertThat(savedEvent.get().getComments()).isEmpty();
    }

    @Test
    void testAddCommentToEvent() {
        Event event = Event.builder()
                .id(eventId)
                .createdDate(createdDate)
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Commentable event")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .comments(new ArrayList<>())
                .build();
        Comment comment = Comment.builder()
                .authorId(UUID.randomUUID())
                .createdDate(LocalDateTime.now())
                .content("Comentario persistido")
                .build();
        eventPersistence.create(event);

        Comment persistedComment = eventPersistence.addComment(eventId, comment);

        Optional<EventEntity> savedEvent = eventRepository.findById(eventId);
        assertThat(persistedComment.getContent()).isEqualTo("Comentario persistido");
        assertThat(savedEvent).isPresent();
        assertThat(savedEvent.get().getComments()).hasSize(1);
        assertThat(savedEvent.get().getComments().getFirst().getContent()).isEqualTo("Comentario persistido");
        assertThat(savedEvent.get().getComments().getFirst().getAuthorId()).isEqualTo(comment.getAuthorId());
    }

    @Test
    void testDeleteEvent() {
        // Arrange
        Event event = Event.builder()
                .id(eventId)
                .createdDate(createdDate)
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Event to delete")
                .description("Test Description")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .comments(new ArrayList<>())
                .build();
        eventPersistence.create(event);

        // Verify event was created
        Optional<EventEntity> savedEvent = eventRepository.findById(eventId);
        assertThat(savedEvent).isPresent();

        // Act
        eventPersistence.delete(eventId);

        // Assert
        Optional<EventEntity> deletedEvent = eventRepository.findById(eventId);
        assertThat(deletedEvent).isEmpty();
    }

    @Test
    void testDeleteNonExistentEvent() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();

        // Assert - Should not throw exception (MongoDB is idempotent)
        eventPersistence.delete(nonExistentId);

        // Verify nothing was deleted
        assertThat(eventRepository.count()).isEqualTo(0);
    }

    @Test
    void testDeleteEventWithComments() {
        // Arrange
        Event event = Event.builder()
                .id(eventId)
                .createdDate(createdDate)
                .eventDate(eventDate)
                .type(EventType.PHASES)
                .title("Event with comments to delete")
                .status(Status.IN_PROGRESS)
                .engagementLetterId(engagementLetterId)
                .build();
        eventPersistence.create(event);

        // Add comments to event
        List<CommentEntity> commentEntities = new ArrayList<>();
        commentEntities.add(CommentEntity.builder()
                .authorId(authorId)
                .createdDate(LocalDateTime.now())
                .content("Comment 1")
                .build());
        commentEntities.add(CommentEntity.builder()
                .authorId(authorId)
                .createdDate(LocalDateTime.now())
                .content("Comment 2")
                .build());

        EventEntity eventEntity = eventRepository.findById(eventId).isPresent()
                ? eventRepository.findById(eventId).get()
                : null;
        Assertions.assertNotNull(eventEntity);
        eventEntity.setComments(commentEntities);
        eventRepository.save(eventEntity);

        // Verify event with comments was created
        Optional<EventEntity> savedEvent = eventRepository.findById(eventId);
        assertThat(savedEvent).isPresent();
        assertThat(savedEvent.get().getComments()).hasSize(2);

        // Act
        eventPersistence.delete(eventId);

        // Assert
        Optional<EventEntity> deletedEvent = eventRepository.findById(eventId);
        assertThat(deletedEvent).isEmpty();
    }

    @Test
    void testDeleteMultipleEvents() {
        // Arrange
        UUID eventId1 = UUID.randomUUID();
        UUID eventId2 = UUID.randomUUID();
        UUID eventId3 = UUID.randomUUID();

        Event event1 = Event.builder()
                .id(eventId1)
                .createdDate(createdDate)
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Event 1")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();

        Event event2 = Event.builder()
                .id(eventId2)
                .createdDate(createdDate)
                .eventDate(eventDate)
                .type(EventType.PHASES)
                .title("Event 2")
                .status(Status.IN_PROGRESS)
                .engagementLetterId(engagementLetterId)
                .build();

        Event event3 = Event.builder()
                .id(eventId3)
                .createdDate(createdDate)
                .eventDate(eventDate)
                .type(EventType.STANDARD_EVENT)
                .title("Event 3")
                .status(Status.COMPLETED)
                .engagementLetterId(engagementLetterId)
                .build();

        eventPersistence.create(event1);
        eventPersistence.create(event2);
        eventPersistence.create(event3);

        assertThat(eventRepository.count()).isEqualTo(3);

        // Act
        eventPersistence.delete(eventId1);
        eventPersistence.delete(eventId3);

        // Assert
        assertThat(eventRepository.count()).isEqualTo(1);
        assertThat(eventRepository.findById(eventId1)).isEmpty();
        assertThat(eventRepository.findById(eventId2)).isPresent();
        assertThat(eventRepository.findById(eventId3)).isEmpty();
    }

    @Test
    void testUpdateEventAllFields() {
        // Arrange
        Event event = Event.builder()
                .id(eventId)
                .createdDate(createdDate)
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Original title")
                .description("Original description")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();
        eventPersistence.create(event);

        Event updatedEvent = Event.builder()
                .id(eventId)
                .eventDate(eventDate.plusDays(1))
                .type(EventType.PHASES)
                .title("Updated title")
                .description("Updated description")
                .status(Status.IN_PROGRESS)
                .engagementLetterId(engagementLetterId)
                .build();

        // Act
        eventPersistence.update(eventId, updatedEvent);

        // Assert
        Optional<EventEntity> retrievedEvent = eventRepository.findById(eventId);
        assertThat(retrievedEvent).isPresent();
        assertThat(retrievedEvent.get().getType()).isEqualTo(EventType.PHASES);
        assertThat(retrievedEvent.get().getTitle()).isEqualTo("Updated title");
        assertThat(retrievedEvent.get().getDescription()).isEqualTo("Updated description");
        assertThat(retrievedEvent.get().getStatus()).isEqualTo(Status.IN_PROGRESS);
    }

    @Test
    void testUpdateEventPreservesCreatedDate() {
        // Arrange
        Event event = Event.builder()
                .id(eventId)
                .createdDate(createdDate)
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Original title")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();
        eventPersistence.create(event);

        Event updatedEvent = Event.builder()
                .id(eventId)
                .eventDate(eventDate.plusDays(1))
                .type(EventType.PHASES)
                .title("Updated title")
                .status(Status.IN_PROGRESS)
                .engagementLetterId(engagementLetterId)
                .build();

        // Act
        eventPersistence.update(eventId, updatedEvent);

        // Assert - createdDate should be preserved (use isCloseTo due to MongoDB precision loss)
        Optional<EventEntity> retrievedEvent = eventRepository.findById(eventId);
        assertThat(retrievedEvent).isPresent();
        assertThat(retrievedEvent.get().getCreatedDate())
                .isCloseTo(createdDate, within(1, ChronoUnit.MILLIS));
    }

    @Test
    void testUpdateEventPreservesEngagementLetterId() {
        // Arrange
        Event event = Event.builder()
                .id(eventId)
                .createdDate(createdDate)
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Original title")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();
        eventPersistence.create(event);

        UUID differentId = UUID.randomUUID();
        Event updatedEvent = Event.builder()
                .id(eventId)
                .eventDate(eventDate.plusDays(1))
                .type(EventType.PHASES)
                .title("Updated title")
                .status(Status.IN_PROGRESS)
                .engagementLetterId(differentId)
                .build();

        // Act
        eventPersistence.update(eventId, updatedEvent);

        // Assert - engagementLetterId should be preserved from original
        Optional<EventEntity> retrievedEvent = eventRepository.findById(eventId);
        assertThat(retrievedEvent).isPresent();
        assertThat(retrievedEvent.get().getEngagementLetterId()).isEqualTo(engagementLetterId);
    }

    @Test
    void testUpdateEventPreservesComments() {
        // Arrange
        Event event = Event.builder()
                .id(eventId)
                .createdDate(createdDate)
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Event with comment")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();
        eventPersistence.create(event);

        // Add comment manually
        EventEntity eventEntity = eventRepository.findById(eventId).isPresent()
                ? eventRepository.findById(eventId).get()
                : null;
        Assertions.assertNotNull(eventEntity);

        List<CommentEntity> commentEntities = new ArrayList<>();
        commentEntities.add(CommentEntity.builder()
                .authorId(authorId)
                .createdDate(LocalDateTime.now())
                .content("Comment 1")
                .build());
        eventEntity.setComments(commentEntities);
        eventRepository.save(eventEntity);

        Event updatedEvent = Event.builder()
                .id(eventId)
                .type(EventType.PHASES)
                .title("Updated title")
                .status(Status.IN_PROGRESS)
                .engagementLetterId(engagementLetterId)
                .build();

        // Act
        eventPersistence.update(eventId, updatedEvent);

        // Assert - Comments should be preserved
        Optional<EventEntity> retrievedEvent = eventRepository.findById(eventId);
        assertThat(retrievedEvent).isPresent();
        assertThat(retrievedEvent.get().getComments()).hasSize(1);
        assertThat(retrievedEvent.get().getComments().getFirst().getContent()).isEqualTo("Comment 1");
    }

    @Test
    void testUpdateNonExistentEvent_ShouldFail() {
        // Arrange
        Event event = Event.builder()
                .id(UUID.randomUUID())
                .type(EventType.MILESTONE)
                .title("Event")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();

        // Act & Assert
        assertThatThrownBy(() -> eventPersistence.update(event.getId(), event))
                .hasMessageContaining("The Event ID doesn't exist");
    }

    @Test
    void testReadEventById() {
        // Arrange
        Event event = Event.builder()
                .id(eventId)
                .createdDate(createdDate)
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Event to read")
                .description("Test Description")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();
        eventPersistence.create(event);

        // Act
        Event retrievedEvent = eventPersistence.readById(eventId);

        // Assert
        assertThat(retrievedEvent).isNotNull();
        assertThat(retrievedEvent.getId()).isEqualTo(eventId);
        assertThat(retrievedEvent.getTitle()).isEqualTo("Event to read");
        assertThat(retrievedEvent.getType()).isEqualTo(EventType.MILESTONE);
    }

    @Test
    void testReadNonExistentEvent_ShouldFail() {
        // Act & Assert
        assertThatThrownBy(() -> eventPersistence.readById(UUID.randomUUID()))
                .hasMessageContaining("The Event ID doesn't exist");
    }

    @Test
    void testFindByEngagementLetterId() {
        // Arrange
        Event event1 = Event.builder()
                .id(UUID.randomUUID())
                .createdDate(createdDate)
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Event 1")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();
        Event event2 = Event.builder()
                .id(UUID.randomUUID())
                .createdDate(createdDate)
                .eventDate(eventDate.plusDays(1))
                .type(EventType.PHASES)
                .title("Event 2")
                .status(Status.IN_PROGRESS)
                .engagementLetterId(engagementLetterId)
                .build();

        eventPersistence.create(event1);
        eventPersistence.create(event2);

        // Act
        var events = eventPersistence.findByEngagementLetterId(engagementLetterId).toList();

        // Assert
        assertThat(events).hasSize(2);
        assertThat(events.get(0).getTitle()).isEqualTo("Event 1");
        assertThat(events.get(1).getTitle()).isEqualTo("Event 2");
    }

    @Test
    void testFindByEngagementLetterId_OrderedByDate() {
        // Arrange - Create events with different dates
        Event eventLater = Event.builder()
                .id(UUID.randomUUID())
                .createdDate(createdDate)
                .eventDate(eventDate.plusDays(3))
                .type(EventType.PHASES)
                .title("Event Later")
                .status(Status.IN_PROGRESS)
                .engagementLetterId(engagementLetterId)
                .build();
        Event eventEarlier = Event.builder()
                .id(UUID.randomUUID())
                .createdDate(createdDate)
                .eventDate(eventDate.plusDays(1))
                .type(EventType.MILESTONE)
                .title("Event Earlier")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();

        eventPersistence.create(eventLater);
        eventPersistence.create(eventEarlier);

        // Act
        var events = eventPersistence.findByEngagementLetterId(engagementLetterId).toList();

        // Assert - Should be ordered by eventDate ASC
        assertThat(events).hasSize(2);
        assertThat(events.get(0).getTitle()).isEqualTo("Event Earlier");
        assertThat(events.get(1).getTitle()).isEqualTo("Event Later");
    }

    @Test
    void testFindByEngagementLetterId_Empty() {
        // Act
        var events = eventPersistence.findByEngagementLetterId(UUID.randomUUID()).toList();

        // Assert
        assertThat(events).isEmpty();
    }

    @Test
    void testFindByEngagementLetterId_MultipleLetterdIds() {
        // Arrange
        UUID anotherEngagementLetterId = UUID.randomUUID();

        Event eventForLetter1 = Event.builder()
                .id(UUID.randomUUID())
                .createdDate(createdDate)
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Event for Letter 1")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();
        Event eventForLetter2 = Event.builder()
                .id(UUID.randomUUID())
                .createdDate(createdDate)
                .eventDate(eventDate)
                .type(EventType.PHASES)
                .title("Event for Letter 2")
                .status(Status.IN_PROGRESS)
                .engagementLetterId(anotherEngagementLetterId)
                .build();

        eventPersistence.create(eventForLetter1);
        eventPersistence.create(eventForLetter2);

        // Act
        var eventsForLetter1 = eventPersistence.findByEngagementLetterId(engagementLetterId).toList();
        var eventsForLetter2 = eventPersistence.findByEngagementLetterId(anotherEngagementLetterId).toList();

        // Assert - Each query should return only events for that specific letter
        assertThat(eventsForLetter1).hasSize(1);
        assertThat(eventsForLetter1.getFirst().getTitle()).isEqualTo("Event for Letter 1");

        assertThat(eventsForLetter2).hasSize(1);
        assertThat(eventsForLetter2.getFirst().getTitle()).isEqualTo("Event for Letter 2");
    }
}

