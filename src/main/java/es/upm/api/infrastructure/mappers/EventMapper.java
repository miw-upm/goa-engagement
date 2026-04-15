package es.upm.api.infrastructure.mappers;

import es.upm.api.domain.model.Comment;
import es.upm.api.domain.model.Event;
import es.upm.api.infrastructure.dtos.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class EventMapper {

    //Traducir datos entrada de usuario a entidad
    public Event toEntity(EventCreateDto dto) {
        if (dto == null) return null;
        return Event.builder()
                .type(dto.getType())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .eventDate(dto.getEventDate())
                .status(dto.getStatus())
                .engagementLetterId(dto.getEngagementLetterId())
                .comments(new ArrayList<>())
                .build();
    }

    //Traducir datos de entidad a salida
    public EventResponseDto toDto(Event event) {
        if (event == null) {
            return null;
        }

        return EventResponseDto.builder()
                .id(event.getId())
                .createdDate(event.getCreatedDate())
                .eventDate(event.getEventDate())
                .type(event.getType())
                .title(event.getTitle())
                .description(event.getDescription())
                .status(event.getStatus())
                .engagementLetterId(event.getEngagementLetterId())
                .comments(toCommentDtoList(event.getComments()))
                .build();
    }

    private List<CommentDto> toCommentDtoList(List<Comment> comments) {
        return Optional.ofNullable(comments)
                .orElse(Collections.emptyList())
                .stream()
                .map(this::toCommentDto)
                .collect(Collectors.toList());
    }

    private CommentDto toCommentDto(Comment comment) {
        if (comment == null) {
            return null;
        }

        return CommentDto.builder()
                .authorId(comment.getAuthorId())
                .createdDate(comment.getCreatedDate())
                .content(comment.getContent())
                .build();
    }

    public Event updateEntity(Event existingEvent, EventUpdateDto dto) {
        if (dto == null || existingEvent == null) {
            return existingEvent;
        }

        if (dto.getType() != null) {
            existingEvent.setType(dto.getType());
        }
        if (dto.getTitle() != null) {
            existingEvent.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null) {
            existingEvent.setDescription(dto.getDescription());
        }
        if (dto.getEventDate() != null) {
            existingEvent.setEventDate(dto.getEventDate());
        }
        if (dto.getStatus() != null) {
            existingEvent.setStatus(dto.getStatus());
        }

        return existingEvent;
    }

    public List<EventResponseDto> toDtoList(List<Event> events) {
        return Optional.ofNullable(events)
                .orElse(Collections.emptyList())
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    public TimelineEventDto toTimelineDto(Event event) {
        if (event == null) {
            return null;
        }

        return TimelineEventDto.builder()
                .id(event.getId())
                .date(event.getEventDate())
                .type(event.getType())
                .title(event.getTitle())
                .description(event.getDescription())
                .status(event.getStatus())
                .build();
    }

    public List<TimelineEventDto> toTimelineDtoList(List<Event> events) {
        return Optional.ofNullable(events)
                .orElse(Collections.emptyList())
                .stream()
                .map(this::toTimelineDto)
                .collect(Collectors.toList());
    }
}
