package es.upm.api.infrastructure.resources;

import es.upm.api.domain.model.Alert;
import es.upm.api.domain.services.AlertService;
import es.upm.api.infrastructure.dtos.AlertCreateDto;
import es.upm.api.infrastructure.dtos.AlertResponseDto;
import es.upm.api.infrastructure.dtos.AlertUpdateDto;
import es.upm.api.infrastructure.mappers.AlertMapper;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/{alertId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Read alert by id")
    public AlertResponseDto readById(@PathVariable UUID alertId) {
        Alert alert = this.alertService.readById(alertId);
        return this.alertMapper.toDto(alert);
    }
}
