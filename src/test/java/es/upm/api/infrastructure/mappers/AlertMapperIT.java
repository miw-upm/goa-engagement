package es.upm.api.infrastructure.mappers;

import es.upm.api.domain.model.Alert;
import es.upm.api.domain.model.AlertNotification;
import es.upm.api.domain.model.PendingAlertNotification;
import es.upm.api.domain.model.Status;
import es.upm.api.infrastructure.dtos.AlertCreateDto;
import es.upm.api.infrastructure.dtos.AlertNotificationPendingDto;
import es.upm.api.infrastructure.dtos.AlertResponseDto;
import es.upm.api.infrastructure.dtos.AlertSummaryDto;
import es.upm.api.infrastructure.dtos.AlertUpdateDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class AlertMapperIT {

    @Autowired
    private AlertMapper alertMapper;

    @Test
    void testToEntityFromCreateDto() {
        UUID engagementLetterId = UUID.randomUUID();
        LocalDateTime dueDate = LocalDateTime.of(2026, 4, 25, 18, 0);

        AlertCreateDto dto = AlertCreateDto.builder()
                .title("Alert title")
                .description("Alert description")
                .dueDate(dueDate)
                .engagementLetterId(engagementLetterId)
                .build();

        Alert alert = this.alertMapper.toEntity(dto);

        assertThat(alert).isNotNull();
        assertThat(alert.getTitle()).isEqualTo("Alert title");
        assertThat(alert.getDescription()).isEqualTo("Alert description");
        assertThat(alert.getDueDate()).isEqualTo(dueDate);
        assertThat(alert.getEngagementLetterId()).isEqualTo(engagementLetterId);
        assertThat(alert.getNotifications()).isNotNull();
        assertThat(alert.getNotifications()).isEmpty();
    }

    @Test
    void testToEntityFromCreateDtoNull() {
        Alert alert = this.alertMapper.toEntity((AlertCreateDto) null);
        assertThat(alert).isNull();
    }

    @Test
    void testToDtoFromCreatedAlert() {
        UUID alertId = UUID.randomUUID();
        UUID engagementLetterId = UUID.randomUUID();
        LocalDateTime dueDate = LocalDateTime.of(2026, 4, 25, 18, 0);
        LocalDateTime createdAt = LocalDateTime.of(2026, 4, 10, 9, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 4, 12, 10, 30);

        AlertNotification notification1 = AlertNotification.builder()
                .id(UUID.randomUUID())
                .offsetMinutes(-4320)
                .triggerAt(LocalDateTime.of(2026, 4, 22, 18, 0))
                .status(Status.PENDING)
                .shownAt(null)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        AlertNotification notification2 = AlertNotification.builder()
                .id(UUID.randomUUID())
                .offsetMinutes(-120)
                .triggerAt(LocalDateTime.of(2026, 4, 25, 16, 0))
                .status(Status.PENDING)
                .shownAt(null)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        Alert alert = Alert.builder()
                .id(alertId)
                .title("Alert title")
                .description("Alert description")
                .dueDate(dueDate)
                .engagementLetterId(engagementLetterId)
                .status(Status.PENDING)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .createdBy("creator")
                .updatedBy("updater")
                .notifications(List.of(notification1, notification2))
                .build();

        AlertResponseDto dto = this.alertMapper.toDto(alert);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(alertId);
        assertThat(dto.getTitle()).isEqualTo("Alert title");
        assertThat(dto.getDescription()).isEqualTo("Alert description");
        assertThat(dto.getDueDate()).isEqualTo(dueDate);
        assertThat(dto.getEngagementLetterId()).isEqualTo(engagementLetterId);
        assertThat(dto.getStatus()).isEqualTo(Status.PENDING);
        assertThat(dto.getCreatedAt()).isEqualTo(createdAt);
        assertThat(dto.getUpdatedAt()).isEqualTo(updatedAt);
        assertThat(dto.getCreatedBy()).isEqualTo("creator");
        assertThat(dto.getUpdatedBy()).isEqualTo("updater");
        assertThat(dto.getNotifications()).hasSize(2);
        assertThat(dto.getNotifications().get(0).getOffsetMinutes()).isEqualTo(-4320);
        assertThat(dto.getNotifications().get(0).getTriggerAt())
                .isEqualTo(LocalDateTime.of(2026, 4, 22, 18, 0));
        assertThat(dto.getNotifications().get(0).getStatus()).isEqualTo(Status.PENDING);
        assertThat(dto.getNotifications().get(1).getOffsetMinutes()).isEqualTo(-120);
        assertThat(dto.getNotifications().get(1).getTriggerAt())
                .isEqualTo(LocalDateTime.of(2026, 4, 25, 16, 0));
        assertThat(dto.getNotifications().get(1).getStatus()).isEqualTo(Status.PENDING);
    }

    @Test
    void testToPendingNotificationDto() {
        UUID alertId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        UUID engagementLetterId = UUID.randomUUID();
        LocalDateTime dueDate = LocalDateTime.of(2026, 4, 25, 18, 0);
        LocalDateTime triggerAt = LocalDateTime.of(2026, 4, 24, 18, 0);

        Alert alert = Alert.builder()
                .id(alertId)
                .title("Alert title")
                .description("Alert description")
                .dueDate(dueDate)
                .engagementLetterId(engagementLetterId)
                .status(Status.PENDING)
                .build();

        AlertNotification notification = AlertNotification.builder()
                .id(notificationId)
                .offsetMinutes(-1440)
                .triggerAt(triggerAt)
                .status(Status.PENDING)
                .createdAt(LocalDateTime.of(2026, 4, 10, 9, 0))
                .updatedAt(LocalDateTime.of(2026, 4, 12, 10, 30))
                .build();

        PendingAlertNotification pendingAlertNotification = PendingAlertNotification.builder()
                .alert(alert)
                .notification(notification)
                .build();

        AlertNotificationPendingDto dto = this.alertMapper.toPendingNotificationDto(pendingAlertNotification);

        assertThat(dto).isNotNull();
        assertThat(dto.getNotificationId()).isEqualTo(notificationId);
        assertThat(dto.getAlertId()).isEqualTo(alertId);
        assertThat(dto.getOffsetMinutes()).isEqualTo(-1440);
        assertThat(dto.getTriggerAt()).isEqualTo(triggerAt);
        assertThat(dto.getStatus()).isEqualTo(Status.PENDING);
        assertThat(dto.getTitle()).isEqualTo("Alert title");
        assertThat(dto.getDescription()).isEqualTo("Alert description");
        assertThat(dto.getDueDate()).isEqualTo(dueDate);
        assertThat(dto.getEngagementLetterId()).isEqualTo(engagementLetterId);
    }

    @Test
    void testToEntityFromUpdateDto() {
        LocalDateTime dueDate = LocalDateTime.of(2026, 4, 25, 18, 0);

        AlertUpdateDto dto = AlertUpdateDto.builder()
                .title("Updated title")
                .description("Updated description")
                .dueDate(dueDate)
                .build();

        Alert alert = this.alertMapper.toEntity(dto);

        assertThat(alert).isNotNull();
        assertThat(alert.getTitle()).isEqualTo("Updated title");
        assertThat(alert.getDescription()).isEqualTo("Updated description");
        assertThat(alert.getDueDate()).isEqualTo(dueDate);
        assertThat(alert.getEngagementLetterId()).isNull();
        assertThat(alert.getStatus()).isNull();
        assertThat(alert.getNotifications()).isNull();
    }

    @Test
    void testToEntityFromUpdateDtoNull() {
        Alert alert = this.alertMapper.toEntity((AlertUpdateDto) null);
        assertThat(alert).isNull();
    }

    @Test
    void testToDtoFromUpdatedAlert() {
        UUID alertId = UUID.randomUUID();
        UUID engagementLetterId = UUID.randomUUID();
        LocalDateTime dueDate = LocalDateTime.of(2026, 4, 25, 18, 0);
        LocalDateTime triggerAt = dueDate.plusMinutes(-120);
        LocalDateTime now = LocalDateTime.now();

        AlertNotification notification = AlertNotification.builder()
                .id(UUID.randomUUID())
                .offsetMinutes(-120)
                .triggerAt(triggerAt)
                .status(Status.PENDING)
                .shownAt(null)
                .createdAt(now.minusDays(1))
                .updatedAt(now)
                .build();

        Alert alert = Alert.builder()
                .id(alertId)
                .title("Updated title")
                .description("Updated description")
                .dueDate(dueDate)
                .engagementLetterId(engagementLetterId)
                .status(Status.PENDING)
                .createdAt(now.minusDays(2))
                .updatedAt(now)
                .createdBy("creator")
                .updatedBy("admin")
                .notifications(List.of(notification))
                .build();

        AlertResponseDto dto = this.alertMapper.toDto(alert);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(alertId);
        assertThat(dto.getTitle()).isEqualTo("Updated title");
        assertThat(dto.getDescription()).isEqualTo("Updated description");
        assertThat(dto.getDueDate()).isEqualTo(dueDate);
        assertThat(dto.getEngagementLetterId()).isEqualTo(engagementLetterId);
        assertThat(dto.getStatus()).isEqualTo(Status.PENDING);
        assertThat(dto.getCreatedBy()).isEqualTo("creator");
        assertThat(dto.getUpdatedBy()).isEqualTo("admin");
        assertThat(dto.getNotifications()).hasSize(1);
        assertThat(dto.getNotifications().getFirst().getOffsetMinutes()).isEqualTo(-120);
        assertThat(dto.getNotifications().getFirst().getTriggerAt()).isEqualTo(triggerAt);
    }

    @Test
    void testToDtoNullAlert() {
        AlertResponseDto dto = this.alertMapper.toDto(null);
        assertThat(dto).isNull();
    }

    @Test
    void testToSummaryDto() {
        UUID alertId = UUID.randomUUID();
        LocalDateTime dueDate = LocalDateTime.of(2026, 4, 25, 18, 0);

        Alert alert = Alert.builder()
                .id(alertId)
                .title("Alert title")
                .description("Description not included")
                .dueDate(dueDate)
                .status(Status.PENDING)
                .build();

        AlertSummaryDto dto = this.alertMapper.toSummaryDto(alert);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(alertId);
        assertThat(dto.getTitle()).isEqualTo("Alert title");
        assertThat(dto.getDueDate()).isEqualTo(dueDate);
        assertThat(dto.getStatus()).isEqualTo(Status.PENDING);
    }

    @Test
    void testToSummaryDtoNull() {
        AlertSummaryDto dto = this.alertMapper.toSummaryDto(null);
        assertThat(dto).isNull();
    }

    @Test
    void testToSummaryDtoList() {
        Alert alert1 = Alert.builder()
                .id(UUID.randomUUID())
                .title("Alert 1")
                .dueDate(LocalDateTime.of(2026, 4, 25, 18, 0))
                .status(Status.PENDING)
                .build();

        Alert alert2 = Alert.builder()
                .id(UUID.randomUUID())
                .title("Alert 2")
                .dueDate(LocalDateTime.of(2026, 4, 28, 10, 30))
                .status(Status.CANCELLED)
                .build();

        List<AlertSummaryDto> result = this.alertMapper.toSummaryDtoList(List.of(alert1, alert2));

        assertThat(result).hasSize(2);

        assertThat(result.getFirst().getId()).isEqualTo(alert1.getId());
        assertThat(result.getFirst().getTitle()).isEqualTo("Alert 1");
        assertThat(result.getFirst().getDueDate()).isEqualTo(LocalDateTime.of(2026, 4, 25, 18, 0));
        assertThat(result.getFirst().getStatus()).isEqualTo(Status.PENDING);

        assertThat(result.get(1).getId()).isEqualTo(alert2.getId());
        assertThat(result.get(1).getTitle()).isEqualTo("Alert 2");
        assertThat(result.get(1).getDueDate()).isEqualTo(LocalDateTime.of(2026, 4, 28, 10, 30));
        assertThat(result.get(1).getStatus()).isEqualTo(Status.CANCELLED);
    }

    @Test
    void testToSummaryDtoListEmpty() {
        List<AlertSummaryDto> result = this.alertMapper.toSummaryDtoList(List.of());
        assertThat(result).isEmpty();
    }

    @Test
    void testToSummaryDtoListNull() {
        List<AlertSummaryDto> result = this.alertMapper.toSummaryDtoList(null);
        assertThat(result).isEmpty();
    }
}
