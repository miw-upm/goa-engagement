package es.upm.api.infrastructure.mappers;

import es.upm.api.domain.model.Alert;
import es.upm.api.domain.model.AlertNotification;
import es.upm.api.domain.model.Status;
import es.upm.api.infrastructure.dtos.AlertResponseDto;
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
}