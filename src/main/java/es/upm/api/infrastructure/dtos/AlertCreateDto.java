package es.upm.api.infrastructure.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertCreateDto {
    @NotBlank(message = "Alert title is required")
    private String title;
    private String description;
    @NotNull(message = "Due date is required")
    private LocalDateTime dueDate;
    @NotNull(message = "Engagement letter ID is required")
    private UUID engagementLetterId;
}
