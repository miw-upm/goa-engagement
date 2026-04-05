package es.upm.api.infrastructure.mappers;

import es.upm.api.domain.model.Comment;
import es.upm.api.domain.model.Event;
import es.upm.api.domain.model.EventType;
import es.upm.api.domain.model.Status;
import es.upm.api.infrastructure.dtos.CommentCreateDto;
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

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class EventMapperIT {

    @Autowired
    private EventMapper eventMapper;

    private UUID engagementLetterId;
    private LocalDateTime eventDate;

    @BeforeEach
    void setUp() {
        engagementLetterId = UUID.randomUUID();
        eventDate = LocalDateTime.now().plusDays(1);
    }

    @Test
    void testToEntity_FromCreateDto_WithoutComments() {
        // Arrange
        EventCreateDto dto = EventCreateDto.builder()
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Test Event")
                .description("Test Description")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .comments(null)
                .build();

        // Act
        Event event = eventMapper.toEntity(dto);

        // Assert
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
    void testToEntity_FromCreateDto_WithComments() {
        // Arrange
        List<CommentCreateDto> comments = new ArrayList<>();
        comments.add(CommentCreateDto.builder().content("First comment").build());
        comments.add(CommentCreateDto.builder().content("Second comment").build());

        EventCreateDto dto = EventCreateDto.builder()
                .eventDate(eventDate)
                .type(EventType.PHASES)
                .title("Event with Comments")
                .description("Test Description")
                .status(Status.IN_PROGRESS)
                .engagementLetterId(engagementLetterId)
                .comments(comments)
                .build();

        LocalDateTime beforeMapping = LocalDateTime.now();

        // Act
        Event event = eventMapper.toEntity(dto);

        LocalDateTime afterMapping = LocalDateTime.now();

        // Assert
        assertThat(event).isNotNull();
        assertThat(event.getComments()).hasSize(2);
        assertThat(event.getComments().getFirst().getContent()).isEqualTo("First comment");
        assertThat(event.getComments().get(1).getContent()).isEqualTo("Second comment");
        // Verify that createdDate is automatically set
        assertThat(event.getComments().getFirst().getCreatedDate()).isNotNull();
        assertThat(event.getComments().getFirst().getCreatedDate()).isAfterOrEqualTo(beforeMapping);
        assertThat(event.getComments().getFirst().getCreatedDate()).isBeforeOrEqualTo(afterMapping);
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
                .createdDate(commentDate)
                .content("First comment")
                .build());
        comments.add(Comment.builder()
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
        assertThat(dto.getComments().get(0).getContent()).isEqualTo("First comment");
        assertThat(dto.getComments().get(0).getCreatedDate()).isEqualTo(commentDate);
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
        List<CommentCreateDto> commentDtos = new ArrayList<>();
        commentDtos.add(CommentCreateDto.builder().content("Test comment").build());

        EventCreateDto createDto = EventCreateDto.builder()
                .eventDate(eventDate)
                .type(EventType.MILESTONE)
                .title("Test Event")
                .description("Test Description")
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .comments(commentDtos)
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
        assertThat(responseDto.getComments()).hasSize(1);
        assertThat(responseDto.getComments().getFirst().getContent()).isEqualTo("Test comment");
        assertThat(responseDto.getComments().getFirst().getCreatedDate()).isNotNull();
    }
}


