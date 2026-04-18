package es.upm.api.infrastructure.resources;

import es.upm.api.domain.model.Alert;
import es.upm.api.domain.services.AlertService;
import es.upm.api.infrastructure.dtos.*;
import es.upm.api.infrastructure.mappers.AlertMapper;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@PreAuthorize(Security.ADMIN_MANAGER_OPERATOR)
@RequestMapping(AlertResource.ALERTS)
public class AlertResource {
    public static final String ALERTS = "/alerts";

    private final AlertService alertService;
    private final AlertMapper alertMapper;

    public AlertResource(AlertService alertService, AlertMapper alertMapper) {
        this.alertService = alertService;
        this.alertMapper = alertMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create alert")
    public AlertResponseDto create(@Valid @RequestBody AlertCreateDto alertCreateDto,
                                   Authentication authentication) {
        Alert alert = this.alertMapper.toEntity(alertCreateDto);
        Alert createdAlert = this.alertService.create(alert, authentication.getName());
        return this.alertMapper.toDto(createdAlert);
    }

    @PutMapping("/{alertId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update alert")
    public AlertResponseDto update(@PathVariable UUID alertId,
                                   @Valid @RequestBody AlertUpdateDto alertUpdateDto,
                                   Authentication authentication) {
        Alert alert = this.alertMapper.toEntity(alertUpdateDto);
        Alert updatedAlert = this.alertService.update(alertId, alert, authentication.getName());
        return this.alertMapper.toDto(updatedAlert);
    }

    @PutMapping("/{alertId}/notifications")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Configure alert notifications")
    public AlertResponseDto configureNotifications(@PathVariable UUID alertId,
                                                   @Valid @RequestBody AlertNotificationConfigDto alertNotificationConfigDto,
                                                   Authentication authentication) {
        List<Integer> offsetMinutes = this.alertMapper.toOffsetMinutes(alertNotificationConfigDto);
        Alert updatedAlert = this.alertService.configureNotifications(alertId, offsetMinutes, authentication.getName());
        return this.alertMapper.toDto(updatedAlert);
    }

    @GetMapping("/{alertId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Read alert by id")
    public AlertResponseDto readById(@PathVariable UUID alertId) {
        Alert alert = this.alertService.readById(alertId);
        return this.alertMapper.toDto(alert);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "List alerts by engagement letter id")
    public List<AlertSummaryDto> findByEngagementLetterId(@RequestParam UUID engagementLetterId) {
        List<Alert> alerts = this.alertService.findByEngagementLetterId(engagementLetterId);
        return this.alertMapper.toSummaryDtoList(alerts);
    }

    @PatchMapping("/{alertId}/cancel")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Cancel alert")
    public AlertResponseDto cancel(@PathVariable UUID alertId,
                                   Authentication authentication) {
        Alert cancelledAlert = this.alertService.cancel(alertId, authentication.getName());
        return this.alertMapper.toDto(cancelledAlert);
    }
}
