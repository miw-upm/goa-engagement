package es.upm.api.infrastructure.mongodb.persistence;

import es.upm.api.domain.model.Comment;
import es.upm.api.domain.model.Event;
import es.upm.api.domain.model.EventType;
import es.upm.api.domain.model.Status;
import es.upm.api.infrastructure.mongodb.entities.CommentEntity;
import es.upm.api.infrastructure.mongodb.entities.EventEntity;
import es.upm.api.infrastructure.mongodb.repositories.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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
}

