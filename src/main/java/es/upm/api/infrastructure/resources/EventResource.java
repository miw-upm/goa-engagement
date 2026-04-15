package es.upm.api.infrastructure.resources;

import es.upm.api.domain.model.Event;
import es.upm.api.domain.services.EventService;
import es.upm.api.infrastructure.dtos.*;
import es.upm.api.infrastructure.mappers.EventMapper;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@PreAuthorize(Security.ADMIN_MANAGER_OPERATOR)
@RequestMapping(EventResource.EVENTS)
public class EventResource {
    public static final String EVENTS = "/events";
    public static final String ID_ID = "/{id}";
    public static final String ID_COMMENTS = "/{eventId}/comments";
    public static final String ENGAGEMENT_LETTER_ID = "/engagement-letter/{engagementLetterId}";

    private final EventService eventService;
    private final EventMapper eventMapper;

    public EventResource(EventService eventService, EventMapper eventMapper) {
        this.eventService = eventService;
        this.eventMapper = eventMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create event")
    public EventResponseDto create(@Valid @RequestBody EventCreateDto eventCreateDto) {
        Event event = this.eventMapper.toEntity(eventCreateDto);
        Event createdEvent = this.eventService.create(event);
        return this.eventMapper.toDto(createdEvent);
    }

    @GetMapping(ID_ID)
    public EventResponseDto read(@PathVariable UUID id) {
        Event event = this.eventService.readById(id);
        return this.eventMapper.toDto(event);
    }

    @PutMapping(ID_ID)
    @ResponseStatus(HttpStatus.OK)
    public EventResponseDto update(@PathVariable UUID id, @Valid @RequestBody EventUpdateDto eventUpdateDto) {
        Event updatedEvent = this.eventService.update(id, eventUpdateDto);
        return this.eventMapper.toDto(updatedEvent);
    }

    @DeleteMapping(ID_ID)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        this.eventService.delete(id);
    }

    @PostMapping(ID_COMMENTS)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create comment for event")
    public void createComment(@PathVariable UUID eventId,
                              @Valid @RequestBody CommentCreateDto commentCreateDto,
                              Authentication authentication) {
        this.eventService.addComment(
                eventId,
                authentication.getName(),
                commentCreateDto.getContent()
        );
    }

    @DeleteMapping(ID_COMMENTS)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete comment from event")
    public void deleteComment(@PathVariable UUID eventId,
                              @RequestParam UUID authorId,
                              @RequestParam String content,
                              @RequestParam String createdDate,
                              Authentication authentication) {

        LocalDateTime parsedDate = LocalDateTime.parse(createdDate);

        this.eventService.deleteComment(
                eventId,
                authorId,
                parsedDate,
                content,
                authentication.getName()
        );
    }


    @GetMapping(ENGAGEMENT_LETTER_ID)
    @ResponseStatus(HttpStatus.OK)
    public List<EventResponseDto> findByEngagementLetterId(
            @PathVariable UUID engagementLetterId) {
        List<Event> events = this.eventService.findByEngagementLetterId(engagementLetterId).toList();
        return this.eventMapper.toDtoList(events);
    }
    @GetMapping("/{eventId}/comments")
    @ResponseStatus(HttpStatus.OK)
    public List<CommentDto> getComments(@PathVariable UUID eventId) {

        return this.eventService.getComments(eventId);
    }
}
