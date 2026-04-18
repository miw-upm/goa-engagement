package es.upm.api.infrastructure.mongodb.entities;

import es.upm.api.domain.model.Alert;
import es.upm.api.domain.model.Status;
import lombok.*;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document
public class AlertEntity {
    @Id
    private UUID id;
    private String title;
    private String description;
    private LocalDateTime dueDate;
    private UUID engagementLetterId;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private List<AlertNotificationEntity> notifications;

    public AlertEntity(Alert alert) {
        BeanUtils.copyProperties(alert, this);
        this.notifications = alert.getNotifications() == null ? new ArrayList<>() : alert.getNotifications().stream()
                .map(AlertNotificationEntity::new)
                .toList();
    }

    public Alert toAlert() {
        Alert alert = new Alert();
        BeanUtils.copyProperties(this, alert);
        alert.setNotifications(this.notifications == null ? new ArrayList<>() : this.notifications.stream()
                .map(AlertNotificationEntity::toAlertNotification)
                .toList());
        return alert;
    }
}
