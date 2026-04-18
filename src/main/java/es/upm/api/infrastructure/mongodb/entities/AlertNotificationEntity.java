package es.upm.api.infrastructure.mongodb.entities;

import es.upm.api.domain.model.AlertNotification;
import es.upm.api.domain.model.Status;
import lombok.*;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AlertNotificationEntity {
    private UUID id;
    private Integer offsetMinutes;
    private LocalDateTime triggerAt;
    private Status status;
    private LocalDateTime shownAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AlertNotificationEntity(AlertNotification alertNotification) {
        BeanUtils.copyProperties(alertNotification, this);
    }

    public AlertNotification toAlertNotification() {
        AlertNotification alertNotification = new AlertNotification();
        BeanUtils.copyProperties(this, alertNotification);
        return alertNotification;
    }
}
