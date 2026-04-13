package es.upm.api.infrastructure.mappers;

import es.upm.api.domain.model.Comment;
import es.upm.api.domain.model.Event;
import es.upm.api.domain.model.EventType;
import es.upm.api.domain.model.Status;
import es.upm.api.infrastructure.dtos.EventCreateDto;
import es.upm.api.infrastructure.dtos.EventResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class EventMapperIT {

    @Autowired
    private EventMapper eventMapper;

    private UUID engagementLetterId;
    private LocalDateTime eventDate;
    private UUID authorId;

    @BeforeEach
    void setUp() {
        engagementLetterId = UUID.randomUUID();
        eventDate = LocalDateTime.now().plusDays(1);
        authorId = UUID.randomUUID();
    }

    @Test
    void testToEntity_FromCreateDto() {
        EventCreateDto dto = EventCreateDto.builder()
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Test Event")
                .description("Test Description")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();

        Event event = eventMapper.toEntity(dto);

        assertThat(event).isNotNull();
        assertThat(event.getId()).isNull();
        assertThat(event.getCreatedDate()).isNull();
        assertThat(event.getEventDate()).isEqualTo(eventDate);
        assertThat(event.getType()).isEqualTo(EventType.MILESTONE);
        assertThat(event.getTitle()).isEqualTo("Test Event");
        assertThat(event.getDescription()).isEqualTo("Test Description");
        assertThat(event.getStatus()).isEqualTo(Status.PENDING);
        assertThat(event.getEngagementLetterId()).isEqualTo(engagementLetterId);
        assertThat(event.getComments()).isEmpty();
    }

    @Test
    void testToEntity_FromCreateDto_NullDto() {
        // Act
        Event event = eventMapper.toEntity(null);

        // Assert
        assertThat(event).isNull();
    }

    @Test
    void testToEntity_AllEventTypes() {
        // Test MILESTONE
        EventCreateDto milestoneDto = EventCreateDto.builder()
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Milestone")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();

        Event milestoneEvent = eventMapper.toEntity(milestoneDto);
        assertThat(milestoneEvent.getType()).isEqualTo(EventType.MILESTONE);

        // Test PHASES
        EventCreateDto phasesDto = EventCreateDto.builder()
                .eventDate(eventDate)
                .type(EventType.PHASES)
                .title("Phases")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();

        Event phasesEvent = eventMapper.toEntity(phasesDto);
        assertThat(phasesEvent.getType()).isEqualTo(EventType.PHASES);

        // Test STANDARD_EVENT
        EventCreateDto standardDto = EventCreateDto.builder()
                .eventDate(eventDate)
                .type(EventType.STANDARD_EVENT)
                .title("Standard")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();

        Event standardEvent = eventMapper.toEntity(standardDto);
        assertThat(standardEvent.getType()).isEqualTo(EventType.STANDARD_EVENT);
    }

    @Test
    void testToEntity_AllStatus() {
        // Test PENDING
        EventCreateDto pendingDto = EventCreateDto.builder()
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Pending")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();

        Event pendingEvent = eventMapper.toEntity(pendingDto);
        assertThat(pendingEvent.getStatus()).isEqualTo(Status.PENDING);

        // Test IN_PROGRESS
        EventCreateDto inProgressDto = EventCreateDto.builder()
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("In Progress")
                .status(Status.IN_PROGRESS)
                .engagementLetterId(engagementLetterId)
                .build();

        Event inProgressEvent = eventMapper.toEntity(inProgressDto);
        assertThat(inProgressEvent.getStatus()).isEqualTo(Status.IN_PROGRESS);

        // Test COMPLETED
        EventCreateDto completedDto = EventCreateDto.builder()
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Completed")
                .status(Status.COMPLETED)
                .engagementLetterId(engagementLetterId)
                .build();

        Event completedEvent = eventMapper.toEntity(completedDto);
        assertThat(completedEvent.getStatus()).isEqualTo(Status.COMPLETED);

        // Test CANCELLED
        EventCreateDto cancelledDto = EventCreateDto.builder()
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Cancelled")
                .status(Status.CANCELLED)
                .engagementLetterId(engagementLetterId)
                .build();

        Event cancelledEvent = eventMapper.toEntity(cancelledDto);
        assertThat(cancelledEvent.getStatus()).isEqualTo(Status.CANCELLED);
    }

    @Test
    void testToDto_FromEvent_WithoutComments() {
        // Arrange
        UUID eventId = UUID.randomUUID();
        LocalDateTime createdDate = LocalDateTime.now();

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
        EventResponseDto dto = eventMapper.toDto(event);

        // Assert
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(eventId);
        assertThat(dto.getCreatedDate()).isEqualTo(createdDate);
        assertThat(dto.getEventDate()).isEqualTo(eventDate);
        assertThat(dto.getType()).isEqualTo(EventType.MILESTONE);
        assertThat(dto.getTitle()).isEqualTo("Test Event");
        assertThat(dto.getDescription()).isEqualTo("Test Description");
        assertThat(dto.getStatus()).isEqualTo(Status.PENDING);
        assertThat(dto.getEngagementLetterId()).isEqualTo(engagementLetterId);
        assertThat(dto.getComments()).isEmpty();
    }

    @Test
    void testToDto_FromEvent_WithComments() {
        // Arrange
        UUID eventId = UUID.randomUUID();
        LocalDateTime createdDate = LocalDateTime.now();
        LocalDateTime commentDate = LocalDateTime.now();

        List<Comment> comments = new ArrayList<>();
        comments.add(Comment.builder()
                .authorId(authorId)
                .createdDate(commentDate)
                .content("First comment")
                .build());
        comments.add(Comment.builder()
                .authorId(authorId)
                .createdDate(commentDate)
                .content("Second comment")
                .build());

        Event event = Event.builder()
                .id(eventId)
                .createdDate(createdDate)
                .eventDate(eventDate)
                .type(EventType.PHASES)
                .title("Event with Comments")
                .description("Test Description")
                .status(Status.IN_PROGRESS)
                .engagementLetterId(engagementLetterId)
                .comments(comments)
                .build();

        // Act
        EventResponseDto dto = eventMapper.toDto(event);

        // Assert
        assertThat(dto).isNotNull();
        assertThat(dto.getComments()).hasSize(2);
        assertThat(dto.getComments().get(0).getAuthorId()).isEqualTo(authorId);
        assertThat(dto.getComments().get(0).getContent()).isEqualTo("First comment");
        assertThat(dto.getComments().get(0).getCreatedDate()).isEqualTo(commentDate);
        assertThat(dto.getComments().get(1).getAuthorId()).isEqualTo(authorId);
        assertThat(dto.getComments().get(1).getContent()).isEqualTo("Second comment");
        assertThat(dto.getComments().get(1).getCreatedDate()).isEqualTo(commentDate);
    }

    @Test
    void testToDto_FromEvent_NullEvent() {
        // Act
        EventResponseDto dto = eventMapper.toDto(null);

        // Assert
        assertThat(dto).isNull();
    }

    @Test
    void testToDto_AllEventTypes() {
        // Test MILESTONE
        Event milestoneEvent = Event.builder()
                .id(UUID.randomUUID())
                .createdDate(LocalDateTime.now())
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Milestone")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();

        EventResponseDto milestoneDto = eventMapper.toDto(milestoneEvent);
        assertThat(milestoneDto.getType()).isEqualTo(EventType.MILESTONE);

        // Test PHASES
        Event phasesEvent = Event.builder()
                .id(UUID.randomUUID())
                .createdDate(LocalDateTime.now())
                .eventDate(eventDate)
                .type(EventType.PHASES)
                .title("Phases")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();

        EventResponseDto phasesDto = eventMapper.toDto(phasesEvent);
        assertThat(phasesDto.getType()).isEqualTo(EventType.PHASES);

        // Test STANDARD_EVENT
        Event standardEvent = Event.builder()
                .id(UUID.randomUUID())
                .createdDate(LocalDateTime.now())
                .eventDate(eventDate)
                .type(EventType.STANDARD_EVENT)
                .title("Standard")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();

        EventResponseDto standardDto = eventMapper.toDto(standardEvent);
        assertThat(standardDto.getType()).isEqualTo(EventType.STANDARD_EVENT);
    }

    @Test
    void testToDto_AllStatus() {
        // Test PENDING
        Event pendingEvent = Event.builder()
                .id(UUID.randomUUID())
                .createdDate(LocalDateTime.now())
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Pending")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();

        EventResponseDto pendingDto = eventMapper.toDto(pendingEvent);
        assertThat(pendingDto.getStatus()).isEqualTo(Status.PENDING);

        // Test IN_PROGRESS
        Event inProgressEvent = Event.builder()
                .id(UUID.randomUUID())
                .createdDate(LocalDateTime.now())
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("In Progress")
                .status(Status.IN_PROGRESS)
                .engagementLetterId(engagementLetterId)
                .build();

        EventResponseDto inProgressDto = eventMapper.toDto(inProgressEvent);
        assertThat(inProgressDto.getStatus()).isEqualTo(Status.IN_PROGRESS);

        // Test COMPLETED
        Event completedEvent = Event.builder()
                .id(UUID.randomUUID())
                .createdDate(LocalDateTime.now())
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Completed")
                .status(Status.COMPLETED)
                .engagementLetterId(engagementLetterId)
                .build();

        EventResponseDto completedDto = eventMapper.toDto(completedEvent);
        assertThat(completedDto.getStatus()).isEqualTo(Status.COMPLETED);

        // Test CANCELLED
        Event cancelledEvent = Event.builder()
                .id(UUID.randomUUID())
                .createdDate(LocalDateTime.now())
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Cancelled")
                .status(Status.CANCELLED)
                .engagementLetterId(engagementLetterId)
                .build();

        EventResponseDto cancelledDto = eventMapper.toDto(cancelledEvent);
        assertThat(cancelledDto.getStatus()).isEqualTo(Status.CANCELLED);
    }

    @Test
    void testToDto_WithNullDescription() {
        // Arrange
        Event event = Event.builder()
                .id(UUID.randomUUID())
                .createdDate(LocalDateTime.now())
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Event without description")
                .description(null)
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();

        // Act
        EventResponseDto dto = eventMapper.toDto(event);

        // Assert
        assertThat(dto).isNotNull();
        assertThat(dto.getDescription()).isNull();
    }

    @Test
    void testToDto_WithNullComments() {
        // Arrange
        Event event = Event.builder()
                .id(UUID.randomUUID())
                .createdDate(LocalDateTime.now())
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Event with null comments")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .comments(null)
                .build();

        // Act
        EventResponseDto dto = eventMapper.toDto(event);

        // Assert
        assertThat(dto).isNotNull();
        assertThat(dto.getComments()).isEmpty();
    }

    @Test
    void testRoundTripMapping_CreateDtoToEventToResponseDto() {
        // Arrange
        EventCreateDto createDto = EventCreateDto.builder()
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Test Event")
                .description("Test Description")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();

        // Act - First mapping: CreateDto to Entity
        Event event = eventMapper.toEntity(createDto);

        // Simulate what the service does
        event.setId(UUID.randomUUID());
        event.setCreatedDate(LocalDateTime.now());

        // Second mapping: Entity to ResponseDto
        EventResponseDto responseDto = eventMapper.toDto(event);

        // Assert
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getId()).isNotNull();
        assertThat(responseDto.getCreatedDate()).isNotNull();
        assertThat(responseDto.getEventDate()).isEqualTo(eventDate);
        assertThat(responseDto.getType()).isEqualTo(EventType.MILESTONE);
        assertThat(responseDto.getTitle()).isEqualTo("Test Event");
        assertThat(responseDto.getDescription()).isEqualTo("Test Description");
        assertThat(responseDto.getStatus()).isEqualTo(Status.PENDING);
        assertThat(responseDto.getEngagementLetterId()).isEqualTo(engagementLetterId);
        assertThat(responseDto.getComments()).isEmpty();
    }

    @Test
    void testToDto_FromEvent_WithSingleComment() {
        // Arrange
        UUID eventId = UUID.randomUUID();
        LocalDateTime createdDate = LocalDateTime.now();
        LocalDateTime commentDate = LocalDateTime.of(2025, 1, 15, 10, 30, 0);

        List<Comment> comments = new ArrayList<>();
        comments.add(Comment.builder()
                .authorId(authorId)
                .createdDate(commentDate)
                .content("Test comment")
                .build());

        Event event = Event.builder()
                .id(eventId)
                .createdDate(createdDate)
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Event with Comment")
                .description("Description")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .comments(comments)
                .build();

        // Act
        EventResponseDto dto = eventMapper.toDto(event);

        // Assert
        assertThat(dto).isNotNull();
        assertThat(dto.getComments()).hasSize(1);
        assertThat(dto.getComments().get(0).getAuthorId()).isEqualTo(authorId);
        assertThat(dto.getComments().get(0).getCreatedDate()).isEqualTo(commentDate);
        assertThat(dto.getComments().get(0).getContent()).isEqualTo("Test comment");
    }

    @Test
    void testToDto_FromEvent_WithMultipleCommentsDifferentAuthors() {
        // Arrange
        UUID eventId = UUID.randomUUID();
        LocalDateTime createdDate = LocalDateTime.now();
        UUID authorId1 = UUID.randomUUID();
        UUID authorId2 = UUID.randomUUID();
        LocalDateTime commentDate1 = LocalDateTime.of(2025, 1, 15, 10, 30, 0);
        LocalDateTime commentDate2 = LocalDateTime.of(2025, 1, 15, 10, 31, 0);

        List<Comment> comments = new ArrayList<>();
        comments.add(Comment.builder()
                .authorId(authorId1)
                .createdDate(commentDate1)
                .content("Comment by user 1")
                .build());
        comments.add(Comment.builder()
                .authorId(authorId2)
                .createdDate(commentDate2)
                .content("Comment by user 2")
                .build());

        Event event = Event.builder()
                .id(eventId)
                .createdDate(createdDate)
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Event with Multiple Comments")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .comments(comments)
                .build();

        // Act
        EventResponseDto dto = eventMapper.toDto(event);

        // Assert
        assertThat(dto).isNotNull();
        assertThat(dto.getComments()).hasSize(2);
        assertThat(dto.getComments().get(0).getAuthorId()).isEqualTo(authorId1);
        assertThat(dto.getComments().get(0).getCreatedDate()).isEqualTo(commentDate1);
        assertThat(dto.getComments().get(0).getContent()).isEqualTo("Comment by user 1");
        assertThat(dto.getComments().get(1).getAuthorId()).isEqualTo(authorId2);
        assertThat(dto.getComments().get(1).getCreatedDate()).isEqualTo(commentDate2);
        assertThat(dto.getComments().get(1).getContent()).isEqualTo("Comment by user 2");
    }

    @Test
    void testToDto_CommentPreservesExactTimestamp() {
        // Arrange - Use specific timestamp to ensure precision is preserved
        LocalDateTime preciseTimestamp = LocalDateTime.of(2025, 12, 31, 23, 59, 59);

        List<Comment> comments = new ArrayList<>();
        comments.add(Comment.builder()
                .authorId(authorId)
                .createdDate(preciseTimestamp)
                .content("Comment with precise timestamp")
                .build());

        Event event = Event.builder()
                .id(UUID.randomUUID())
                .createdDate(LocalDateTime.now())
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Event")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .comments(comments)
                .build();

        // Act
        EventResponseDto dto = eventMapper.toDto(event);

        // Assert - Timestamp should be preserved exactly
        assertThat(dto.getComments()).hasSize(1);
        assertThat(dto.getComments().get(0).getCreatedDate()).isEqualTo(preciseTimestamp);
        assertThat(dto.getComments().get(0).getCreatedDate().getYear()).isEqualTo(2025);
        assertThat(dto.getComments().get(0).getCreatedDate().getMonthValue()).isEqualTo(12);
        assertThat(dto.getComments().get(0).getCreatedDate().getDayOfMonth()).isEqualTo(31);
        assertThat(dto.getComments().get(0).getCreatedDate().getHour()).isEqualTo(23);
        assertThat(dto.getComments().get(0).getCreatedDate().getMinute()).isEqualTo(59);
        assertThat(dto.getComments().get(0).getCreatedDate().getSecond()).isEqualTo(59);
    }

    @Test
    void testToDto_CommentWithSpecialCharacters() {
        // Arrange
        String contentWithSpecialChars = "Comentario con caracteres especiales: ñ, á, é, í, ó, ú, ü, ¡, ¿";

        List<Comment> comments = new ArrayList<>();
        comments.add(Comment.builder()
                .authorId(authorId)
                .createdDate(LocalDateTime.now())
                .content(contentWithSpecialChars)
                .build());

        Event event = Event.builder()
                .id(UUID.randomUUID())
                .createdDate(LocalDateTime.now())
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Event")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .comments(comments)
                .build();

        // Act
        EventResponseDto dto = eventMapper.toDto(event);

        // Assert
        assertThat(dto.getComments()).hasSize(1);
        assertThat(dto.getComments().get(0).getContent()).isEqualTo(contentWithSpecialChars);
    }

    @Test
    void testToDto_CommentWithLongContent() {
        // Arrange
        String longContent = "A".repeat(1000); // 1000 character comment

        List<Comment> comments = new ArrayList<>();
        comments.add(Comment.builder()
                .authorId(authorId)
                .createdDate(LocalDateTime.now())
                .content(longContent)
                .build());

        Event event = Event.builder()
                .id(UUID.randomUUID())
                .createdDate(LocalDateTime.now())
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Event")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .comments(comments)
                .build();

        // Act
        EventResponseDto dto = eventMapper.toDto(event);

        // Assert
        assertThat(dto.getComments()).hasSize(1);
        assertThat(dto.getComments().get(0).getContent()).hasSize(1000);
        assertThat(dto.getComments().get(0).getContent()).isEqualTo(longContent);
    }

    @Test
    void testToDtoList_WithEventsContainingComments() {
        // Arrange
        UUID authorId1 = UUID.randomUUID();
        UUID authorId2 = UUID.randomUUID();

        List<Comment> comments1 = new ArrayList<>();
        comments1.add(Comment.builder()
                .authorId(authorId1)
                .createdDate(LocalDateTime.now())
                .content("Comment on event 1")
                .build());

        List<Comment> comments2 = new ArrayList<>();
        comments2.add(Comment.builder()
                .authorId(authorId2)
                .createdDate(LocalDateTime.now())
                .content("Comment on event 2")
                .build());

        Event event1 = Event.builder()
                .id(UUID.randomUUID())
                .createdDate(LocalDateTime.now())
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Event 1")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .comments(comments1)
                .build();

        Event event2 = Event.builder()
                .id(UUID.randomUUID())
                .createdDate(LocalDateTime.now())
                .eventDate(eventDate.plusDays(1))
                .type(EventType.PHASES)
                .title("Event 2")
                .status(Status.IN_PROGRESS)
                .engagementLetterId(engagementLetterId)
                .comments(comments2)
                .build();

        List<Event> events = List.of(event1, event2);

        // Act
        List<EventResponseDto> dtos = eventMapper.toDtoList(events);

        // Assert
        assertThat(dtos).hasSize(2);
        assertThat(dtos.get(0).getComments()).hasSize(1);
        assertThat(dtos.get(0).getComments().get(0).getContent()).isEqualTo("Comment on event 1");
        assertThat(dtos.get(1).getComments()).hasSize(1);
        assertThat(dtos.get(1).getComments().get(0).getContent()).isEqualTo("Comment on event 2");
    }

    @Test
    void testToDtoList_EmptyList() {
        // Act
        List<EventResponseDto> dtos = eventMapper.toDtoList(new ArrayList<>());

        // Assert
        assertThat(dtos).isEmpty();
    }

    @Test
    void testToDtoList_NullList() {
        // Act
        List<EventResponseDto> dtos = eventMapper.toDtoList(null);

        // Assert
        assertThat(dtos).isEmpty();
    }

}


