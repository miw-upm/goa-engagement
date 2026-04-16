package es.upm.api.domain.services;

import es.upm.api.domain.exceptions.BadRequestException;
import es.upm.api.domain.model.Alert;
import es.upm.api.domain.model.AlertNotification;
import es.upm.api.domain.model.Status;
import es.upm.api.domain.persistence.AlertPersistence;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AlertService {
    private static final List<Integer> DEFAULT_NOTIFICATION_OFFSETS = List.of(-4320, -1440, -120);

    private final AlertPersistence alertPersistence;
    private final EngagementLetterService engagementLetterService;

    public AlertService(AlertPersistence alertPersistence,
                        EngagementLetterService engagementLetterService) {
        this.alertPersistence = alertPersistence;
        this.engagementLetterService = engagementLetterService;
    }

    public Alert create(Alert alert, String authenticatedUser) {
        LocalDateTime now = LocalDateTime.now();
        alert.setId(UUID.randomUUID());
        alert.setStatus(Status.PENDING);
        alert.setCreatedAt(now);
        alert.setUpdatedAt(now);
        alert.setCreatedBy(authenticatedUser);
        alert.setUpdatedBy(authenticatedUser);

        this.engagementLetterService.readById(alert.getEngagementLetterId());

        if (alert.getNotifications() == null || alert.getNotifications().isEmpty()) {
            alert.setNotifications(DEFAULT_NOTIFICATION_OFFSETS.stream()
                    .map(offsetMinutes -> this.buildNotification(alert, offsetMinutes, now))
                    .toList());
        } else {
            alert.setNotifications(alert.getNotifications().stream()
                    .map(notification -> this.enrichNotification(alert, notification, now))
                    .toList());
        }

        this.alertPersistence.create(alert);
        return alert;
    }

    public Alert update(UUID alertId, Alert alertUpdatedData, String authenticatedUser) {
        Alert existingAlert = this.alertPersistence.readById(alertId);

        if (Status.CANCELLED.equals(existingAlert.getStatus())) {
            throw new BadRequestException("Cancelled alerts cannot be edited");
        }

        boolean dueDateChanged = !existingAlert.getDueDate().equals(alertUpdatedData.getDueDate());
        LocalDateTime now = LocalDateTime.now();

        existingAlert.setTitle(alertUpdatedData.getTitle());
        existingAlert.setDescription(alertUpdatedData.getDescription());
        existingAlert.setDueDate(alertUpdatedData.getDueDate());
        existingAlert.setUpdatedAt(now);
        existingAlert.setUpdatedBy(authenticatedUser);

        if (dueDateChanged && existingAlert.getNotifications() != null) {
            existingAlert.setNotifications(existingAlert.getNotifications().stream()
                    .map(notification -> this.recalculateNotificationTrigger(notification, existingAlert.getDueDate(), now))
                    .toList());
        }

        this.alertPersistence.update(existingAlert);
        return existingAlert;
    }

    public Alert readById(UUID alertId) {
        return this.alertPersistence.readById(alertId);
    }

    public Alert configureNotifications(UUID alertId, List<Integer> offsetMinutes, String authenticatedUser) {
        Alert existingAlert = this.alertPersistence.readById(alertId);

        if (Status.CANCELLED.equals(existingAlert.getStatus())) {
            throw new BadRequestException("Cancelled alerts cannot be configured");
        }

        this.validateOffsetMinutes(offsetMinutes);

        LocalDateTime now = LocalDateTime.now();
        existingAlert.setNotifications(offsetMinutes.stream()
                .map(offset -> this.buildNotification(existingAlert, offset, now))
                .toList());
        existingAlert.setUpdatedAt(now);
        existingAlert.setUpdatedBy(authenticatedUser);

        this.alertPersistence.update(existingAlert);
        return existingAlert;
    }

    public List<Alert> findByEngagementLetterId(UUID engagementLetterId) {
        this.engagementLetterService.readById(engagementLetterId);
        return this.alertPersistence.findByEngagementLetterId(engagementLetterId);
    }

    public Alert cancel(UUID alertId, String authenticatedUser) {
        Alert alert = this.alertPersistence.readById(alertId);
        LocalDateTime now = LocalDateTime.now();

        alert.setStatus(Status.CANCELLED);
        alert.setUpdatedAt(now);
        alert.setUpdatedBy(authenticatedUser);

        if (alert.getNotifications() != null) {
            alert.setNotifications(alert.getNotifications().stream()
                    .map(notification -> this.cancelNotification(notification, now))
                    .toList());
        }

        this.alertPersistence.update(alert);
        return alert;
    }

    private AlertNotification cancelNotification(AlertNotification notification, LocalDateTime now) {
        notification.setStatus(Status.CANCELLED);
        notification.setUpdatedAt(now);
        return notification;
    }
    private AlertNotification buildNotification(Alert alert, Integer offsetMinutes, LocalDateTime now) {
        return AlertNotification.builder()
                .id(UUID.randomUUID())
                .offsetMinutes(offsetMinutes)
                .triggerAt(alert.getDueDate().plusMinutes(offsetMinutes))
                .status(Status.PENDING)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private AlertNotification enrichNotification(Alert alert, AlertNotification notification, LocalDateTime now) {
        notification.setId(UUID.randomUUID());
        notification.setTriggerAt(alert.getDueDate().plusMinutes(notification.getOffsetMinutes()));
        notification.setStatus(Status.PENDING);
        notification.setCreatedAt(now);
        notification.setUpdatedAt(now);
        return notification;
    }

    private AlertNotification recalculateNotificationTrigger(AlertNotification notification,LocalDateTime newDueDate, LocalDateTime now) {
        notification.setTriggerAt(newDueDate.plusMinutes(notification.getOffsetMinutes()));
        notification.setUpdatedAt(now);
        return notification;
    }

    private void validateOffsetMinutes(List<Integer> offsetMinutes) {
        if (offsetMinutes == null) {
            throw new BadRequestException("Offset minutes list is required");
        }
        if (offsetMinutes.stream().anyMatch(offset -> offset == null || offset >= 0)) {
            throw new BadRequestException("Offset minutes must be negative");
        }
        if (offsetMinutes.stream().distinct().count() != offsetMinutes.size()) {
            throw new BadRequestException("Offset minutes cannot be duplicated");
        }
    }
}
