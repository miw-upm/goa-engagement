package es.upm.api.infrastructure.mongodb.persistence;

import es.upm.api.domain.model.Alert;
import es.upm.api.domain.model.AlertNotification;
import es.upm.api.domain.model.Status;
import es.upm.api.infrastructure.mongodb.repositories.AlertRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@ActiveProfiles("test")
class AlertPersistenceMongodbIT {

    @Autowired
    private AlertRepository alertRepository;

    private AlertPersistenceMongodb alertPersistence;

    @BeforeEach
    void setUp() {
        this.alertPersistence = new AlertPersistenceMongodb(this.alertRepository);
        this.alertRepository.deleteAll();
    }

    @Test
    void testUpdateAlert() {
        UUID alertId = UUID.randomUUID();
        UUID engagementLetterId = UUID.randomUUID();

        LocalDateTime createdAt = LocalDateTime.of(2026, 4, 10, 10, 0);
        LocalDateTime initialDueDate = LocalDateTime.of(2026, 4, 20, 10, 0);
        LocalDateTime updatedDueDate = LocalDateTime.of(2026, 4, 25, 18, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 4, 13, 12, 30);

        AlertNotification notification = AlertNotification.builder()
                .id(UUID.randomUUID())
                .offsetMinutes(-120)
                .triggerAt(initialDueDate.plusMinutes(-120))
                .status(Status.PENDING)
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .build();

        Alert alert = Alert.builder()
                .id(alertId)
                .title("Original title")
                .description("Original description")
                .dueDate(initialDueDate)
                .engagementLetterId(engagementLetterId)
                .status(Status.PENDING)
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .createdBy("creator")
                .updatedBy("creator")
                .notifications(List.of(notification))
                .build();

        this.alertPersistence.create(alert);

        Alert toUpdate = this.alertPersistence.readById(alertId);
        toUpdate.setTitle("Updated title");
        toUpdate.setDescription("Updated description");
        toUpdate.setDueDate(updatedDueDate);
        toUpdate.setUpdatedAt(updatedAt);
        toUpdate.setUpdatedBy("admin");
        toUpdate.setNotifications(
                toUpdate.getNotifications().stream()
                        .peek(n -> {
                            n.setTriggerAt(updatedDueDate.plusMinutes(n.getOffsetMinutes()));
                            n.setUpdatedAt(updatedAt);
                        })
                        .toList()
        );

        this.alertPersistence.update(toUpdate);

        Alert updated = this.alertPersistence.readById(alertId);

        assertThat(updated.getId()).isEqualTo(alertId);
        assertThat(updated.getTitle()).isEqualTo("Updated title");
        assertThat(updated.getDescription()).isEqualTo("Updated description");
        assertThat(updated.getDueDate()).isEqualTo(updatedDueDate);
        assertThat(updated.getEngagementLetterId()).isEqualTo(engagementLetterId);
        assertThat(updated.getStatus()).isEqualTo(Status.PENDING);
        assertThat(updated.getCreatedAt()).isEqualTo(createdAt);
        assertThat(updated.getUpdatedAt()).isEqualTo(updatedAt);
        assertThat(updated.getCreatedBy()).isEqualTo("creator");
        assertThat(updated.getUpdatedBy()).isEqualTo("admin");
        assertThat(updated.getNotifications()).hasSize(1);
        assertThat(updated.getNotifications().getFirst().getTriggerAt())
                .isEqualTo(updatedDueDate.plusMinutes(-120));
        assertThat(updated.getNotifications().getFirst().getUpdatedAt()).isEqualTo(updatedAt);
    }
}