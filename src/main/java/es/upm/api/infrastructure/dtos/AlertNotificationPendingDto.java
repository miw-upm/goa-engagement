package es.upm.api.infrastructure.dtos;

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
public class AlertNotificationPendingDto {
    private UUID notificationId;
    private UUID alertId;
    private Integer offsetMinutes;
    private LocalDateTime triggerAt;
    private Status status;
    private String title;
    private String description;
    private LocalDateTime dueDate;
    private UUID engagementLetterId;
}
