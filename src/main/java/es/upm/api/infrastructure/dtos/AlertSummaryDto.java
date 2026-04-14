package es.upm.api.infrastructure.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import es.upm.api.domain.model.Status;
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
public class AlertSummaryDto {
    private UUID id;
    private String title;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dueDate;

    private Status status;
}