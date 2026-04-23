package es.upm.api.infrastructure.resources;

import es.upm.api.domain.model.PendingAlertNotification;
import es.upm.api.domain.services.AlertService;
import es.upm.api.infrastructure.dtos.AlertNotificationPendingDto;
import es.upm.api.infrastructure.mappers.AlertMapper;
import es.upm.miw.security.Security;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@PreAuthorize(Security.ADMIN_MANAGER_OPERATOR)
@RequestMapping(AlertNotificationResource.ALERT_NOTIFICATIONS)
@RequiredArgsConstructor
public class AlertNotificationResource {
    public static final String ALERT_NOTIFICATIONS = "/alert-notifications";

    private final AlertService alertService;
    private final AlertMapper alertMapper;

    @GetMapping("/pending")
    @ResponseStatus(HttpStatus.OK)
    public List<AlertNotificationPendingDto> findPendingNotifications() {
        List<PendingAlertNotification> pendingNotifications = this.alertService.findPendingNotifications();
        return pendingNotifications.stream()
                .map(this.alertMapper::toPendingNotificationDto)
                .toList();
    }

    @PatchMapping("/{notificationId}/shown")
    @ResponseStatus(HttpStatus.OK)
    public void markAsShown(@PathVariable UUID notificationId) {
        this.alertService.markNotificationAsShown(notificationId);
    }
}
