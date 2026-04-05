package es.upm.api.infrastructure.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import es.upm.api.domain.model.EventType;
import es.upm.api.domain.model.Status;
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
public class EventResponseDto {
    private UUID id;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdDate;  // System-generated

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime eventDate;   // User-provided

    private EventType type;

    private String title;

    private String description;

    private Status status;

    private UUID engagementLetterId;

    private List<CommentDto> comments;
}
