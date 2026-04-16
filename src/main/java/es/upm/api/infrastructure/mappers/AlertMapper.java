package es.upm.api.infrastructure.mappers;

import es.upm.api.domain.model.Alert;
import es.upm.api.infrastructure.dtos.*;
import es.upm.api.domain.model.AlertNotification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class AlertMapper {

    public Alert toEntity(AlertCreateDto dto) {
        if (dto == null) return null;
        return Alert.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .dueDate(dto.getDueDate())
                .engagementLetterId(dto.getEngagementLetterId())
                .notifications(new ArrayList<>())
                .build();
    }

    public List<Integer> toOffsetMinutes(AlertNotificationConfigDto dto) {
        return Optional.ofNullable(dto)
                .map(AlertNotificationConfigDto::getOffsetMinutes)
                .orElse(Collections.emptyList());
    }

    public AlertResponseDto toDto(Alert alert) {
        if (alert == null) {
            return null;
        }

        return AlertResponseDto.builder()
                .id(alert.getId())
                .title(alert.getTitle())
                .description(alert.getDescription())
                .dueDate(alert.getDueDate())
                .engagementLetterId(alert.getEngagementLetterId())
                .status(alert.getStatus())
                .createdAt(alert.getCreatedAt())
                .updatedAt(alert.getUpdatedAt())
                .createdBy(alert.getCreatedBy())
                .updatedBy(alert.getUpdatedBy())
                .notifications(toNotificationDtoList(alert.getNotifications()))
                .build();
    }

    private List<AlertNotificationDto> toNotificationDtoList(List<AlertNotification> notifications) {
        return Optional.ofNullable(notifications)
                .orElse(Collections.emptyList())
                .stream()
                .map(this::toNotificationDto)
                .collect(Collectors.toList());
    }

    private AlertNotificationDto toNotificationDto(AlertNotification notification) {
        if (notification == null) {
            return null;
        }

        return AlertNotificationDto.builder()
                .id(notification.getId())
                .offsetMinutes(notification.getOffsetMinutes())
                .triggerAt(notification.getTriggerAt())
                .status(notification.getStatus())
                .shownAt(notification.getShownAt())
                .createdAt(notification.getCreatedAt())
                .updatedAt(notification.getUpdatedAt())
                .build();
    }

    public Alert toEntity(AlertUpdateDto dto) {
        if (dto == null) return null;
        return Alert.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .dueDate(dto.getDueDate())
                .build();
    }

    public AlertSummaryDto toSummaryDto(Alert alert) {
        if (alert == null) {
            return null;
        }

        return AlertSummaryDto.builder()
                .id(alert.getId())
                .title(alert.getTitle())
                .dueDate(alert.getDueDate())
                .status(alert.getStatus())
                .build();
    }

    public List<AlertSummaryDto> toSummaryDtoList(List<Alert> alerts) {
        return Optional.ofNullable(alerts)
                .orElse(Collections.emptyList())
                .stream()
                .map(this::toSummaryDto)
                .collect(Collectors.toList());
    }
}
