package es.upm.api.infrastructure.resources;

import es.upm.api.domain.model.PendingAlertNotification;
import es.upm.api.domain.services.AlertService;
import es.upm.api.infrastructure.dtos.AlertNotificationPendingDto;
import es.upm.api.infrastructure.mappers.AlertMapper;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@PreAuthorize(Security.ADMIN_MANAGER_OPERATOR)
@RequestMapping(AlertNotificationResource.ALERT_NOTIFICATIONS)
public class AlertNotificationResource {
    public static final String ALERT_NOTIFICATIONS = "/alert-notifications";

    private final AlertService alertService;
    private final AlertMapper alertMapper;

    public AlertNotificationResource(AlertService alertService, AlertMapper alertMapper) {
        this.alertService = alertService;
        this.alertMapper = alertMapper;
    }

    @GetMapping("/pending")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "List pending alert notifications")
    public List<AlertNotificationPendingDto> findPendingNotifications() {
        List<PendingAlertNotification> pendingNotifications = this.alertService.findPendingNotifications();
        return pendingNotifications.stream()
                .map(this.alertMapper::toPendingNotificationDto)
                .toList();
    }

    @PatchMapping("/{notificationId}/shown")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Mark alert notification as shown")
    public void markAsShown(@PathVariable UUID notificationId) {
        this.alertService.markNotificationAsShown(notificationId);
    }
}
