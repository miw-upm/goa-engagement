package es.upm.api.infrastructure.dtos;

import es.upm.api.domain.model.Comment;
import es.upm.api.domain.model.EventType;
import es.upm.api.domain.model.Status;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventCreateDto {

    private LocalDateTime eventDate;   // User-provided
    @NotNull(message = "Event type is required")
    private EventType type;
    @NotBlank(message = "Event title is required")
    private String title;
    private String description;
    @NotNull(message = "Status is required")
    private Status status;
    @NotNull(message = "Engagement letter ID is required")
    private UUID engagementLetterId;
    @Valid
    private List<CommentCreateDto> comments;
}
