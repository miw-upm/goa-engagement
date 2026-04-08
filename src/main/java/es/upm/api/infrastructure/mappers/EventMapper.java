package es.upm.api.infrastructure.mappers;

import es.upm.api.domain.model.Comment;
import es.upm.api.domain.model.Event;
import es.upm.api.infrastructure.dtos.CommentDto;
import es.upm.api.infrastructure.dtos.EventCreateDto;
import es.upm.api.infrastructure.dtos.EventResponseDto;
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

}
