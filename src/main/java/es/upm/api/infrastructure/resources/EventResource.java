package es.upm.api.infrastructure.resources;

import es.upm.api.domain.model.Event;
import es.upm.api.domain.services.EventService;
import es.upm.api.infrastructure.dtos.EventCreateDto;
import es.upm.api.infrastructure.dtos.EventResponseDto;
import es.upm.api.infrastructure.mappers.EventMapper;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@PreAuthorize(Security.ADMIN_MANAGER_OPERATOR)
@RequestMapping(EventResource.EVENTS)
public class EventResource {
    public static final String EVENTS = "/events";

    private final EventService eventService;
    private final EventMapper eventMapper;

    public EventResource(EventService eventService, EventMapper eventMapper) {
        this.eventService = eventService;
        this.eventMapper = eventMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create event")
    public EventResponseDto create(@Valid @RequestBody EventCreateDto eventCreateDto){
        Event event = this.eventMapper.toEntity(eventCreateDto);
        Event createdEvent = this.eventService.create(event);
        return this.eventMapper.toDto(createdEvent);
    }
}
