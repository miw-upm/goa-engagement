package es.upm.api.domain.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    private UUID id;
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDate date;
    @NotNull
    private EventType type;
    @NotBlank
    private String Title;
    private String description;
    @NotNull
    private Status status;
    @NotNull
    private UUID engagementLetterId;
    private List<Comment> comments;
}
