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
    void testCreateAlert() {
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
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        AlertNotification notification2 = AlertNotification.builder()
                .id(UUID.randomUUID())
                .offsetMinutes(-1440)
                .triggerAt(LocalDateTime.of(2026, 4, 24, 18, 0))
                .status(Status.PENDING)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        AlertNotification notification3 = AlertNotification.builder()
                .id(UUID.randomUUID())
                .offsetMinutes(-120)
                .triggerAt(LocalDateTime.of(2026, 4, 25, 16, 0))
                .status(Status.PENDING)
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
                .notifications(List.of(notification1, notification2, notification3))
                .build();

        this.alertPersistence.create(alert);

        Alert result = this.alertPersistence.readById(alertId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(alertId);
        assertThat(result.getTitle()).isEqualTo("Alert title");
        assertThat(result.getDescription()).isEqualTo("Alert description");
        assertThat(result.getDueDate()).isEqualTo(dueDate);
        assertThat(result.getEngagementLetterId()).isEqualTo(engagementLetterId);
        assertThat(result.getStatus()).isEqualTo(Status.PENDING);
        assertThat(result.getCreatedAt()).isEqualTo(createdAt);
        assertThat(result.getUpdatedAt()).isEqualTo(updatedAt);
        assertThat(result.getCreatedBy()).isEqualTo("creator");
        assertThat(result.getUpdatedBy()).isEqualTo("updater");
        assertThat(result.getNotifications()).hasSize(3);
        assertThat(result.getNotifications().get(0).getOffsetMinutes()).isEqualTo(-4320);
        assertThat(result.getNotifications().get(0).getTriggerAt())
                .isEqualTo(LocalDateTime.of(2026, 4, 22, 18, 0));
        assertThat(result.getNotifications().get(0).getStatus()).isEqualTo(Status.PENDING);
        assertThat(result.getNotifications().get(1).getOffsetMinutes()).isEqualTo(-1440);
        assertThat(result.getNotifications().get(1).getTriggerAt())
                .isEqualTo(LocalDateTime.of(2026, 4, 24, 18, 0));
        assertThat(result.getNotifications().get(1).getStatus()).isEqualTo(Status.PENDING);
        assertThat(result.getNotifications().get(2).getOffsetMinutes()).isEqualTo(-120);
        assertThat(result.getNotifications().get(2).getTriggerAt())
                .isEqualTo(LocalDateTime.of(2026, 4, 25, 16, 0));
        assertThat(result.getNotifications().get(2).getStatus()).isEqualTo(Status.PENDING);
    }

    @Test
    void testUpdateAlert() {
        UUID alertId = UUID.randomUUID();
        UUID engagementLetterId = UUID.randomUUID();

        LocalDateTime createdAt = LocalDateTime.of(2026, 4, 10, 10, 0);
        LocalDateTime initialDueDate = LocalDateTime.of(2026, 4, 20, 10, 0);
        LocalDateTime updatedDueDate = LocalDateTime.of(2026, 4, 25, 18, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 4, 13, 12, 30);
        LocalDateTime shownAt = LocalDateTime.of(2026, 4, 13, 12, 0);

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
                            n.setStatus(Status.COMPLETED);
                            n.setShownAt(shownAt);
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
        assertThat(updated.getNotifications().getFirst().getStatus()).isEqualTo(Status.COMPLETED);
        assertThat(updated.getNotifications().getFirst().getShownAt()).isEqualTo(shownAt);
        assertThat(updated.getNotifications().getFirst().getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void testReadById() {
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

        this.alertPersistence.create(alert);

        Alert result = this.alertPersistence.readById(alertId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(alertId);
        assertThat(result.getTitle()).isEqualTo("Alert title");
        assertThat(result.getDescription()).isEqualTo("Alert description");
        assertThat(result.getDueDate()).isEqualTo(dueDate);
        assertThat(result.getEngagementLetterId()).isEqualTo(engagementLetterId);
        assertThat(result.getStatus()).isEqualTo(Status.PENDING);
        assertThat(result.getCreatedAt()).isEqualTo(createdAt);
        assertThat(result.getUpdatedAt()).isEqualTo(updatedAt);
        assertThat(result.getCreatedBy()).isEqualTo("creator");
        assertThat(result.getUpdatedBy()).isEqualTo("updater");
        assertThat(result.getNotifications()).hasSize(2);
        assertThat(result.getNotifications().get(0).getOffsetMinutes()).isEqualTo(-4320);
        assertThat(result.getNotifications().get(0).getTriggerAt())
                .isEqualTo(LocalDateTime.of(2026, 4, 22, 18, 0));
        assertThat(result.getNotifications().get(1).getOffsetMinutes()).isEqualTo(-120);
        assertThat(result.getNotifications().get(1).getTriggerAt())
                .isEqualTo(LocalDateTime.of(2026, 4, 25, 16, 0));
    }

    @Test
    void testFindAll() {
        UUID engagementLetterId1 = UUID.randomUUID();
        UUID engagementLetterId2 = UUID.randomUUID();

        Alert alert1 = Alert.builder()
                .id(UUID.randomUUID())
                .title("Alert 1")
                .description("Description 1")
                .dueDate(LocalDateTime.of(2026, 4, 25, 18, 0))
                .engagementLetterId(engagementLetterId1)
                .status(Status.PENDING)
                .build();

        Alert alert2 = Alert.builder()
                .id(UUID.randomUUID())
                .title("Alert 2")
                .description("Description 2")
                .dueDate(LocalDateTime.of(2026, 4, 28, 10, 30))
                .engagementLetterId(engagementLetterId2)
                .status(Status.CANCELLED)
                .build();

        this.alertPersistence.create(alert1);
        this.alertPersistence.create(alert2);

        List<Alert> result = this.alertPersistence.findAll();

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Alert::getTitle)
                .containsExactlyInAnyOrder("Alert 1", "Alert 2");
        assertThat(result)
                .extracting(Alert::getDescription)
                .containsExactlyInAnyOrder("Description 1", "Description 2");
        assertThat(result)
                .extracting(Alert::getEngagementLetterId)
                .containsExactlyInAnyOrder(engagementLetterId1, engagementLetterId2);
    }

    @Test
    void testFindByEngagementLetterId() {
        UUID engagementLetterId1 = UUID.randomUUID();
        UUID engagementLetterId2 = UUID.randomUUID();

        Alert alert1 = Alert.builder()
                .id(UUID.randomUUID())
                .title("Alert 1")
                .dueDate(LocalDateTime.of(2026, 4, 25, 18, 0))
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId1)
                .build();

        Alert alert2 = Alert.builder()
                .id(UUID.randomUUID())
                .title("Alert 2")
                .dueDate(LocalDateTime.of(2026, 4, 28, 10, 30))
                .status(Status.CANCELLED)
                .engagementLetterId(engagementLetterId1)
                .build();

        Alert alert3 = Alert.builder()
                .id(UUID.randomUUID())
                .title("Alert 3")
                .dueDate(LocalDateTime.of(2026, 5, 1, 12, 0))
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId2)
                .build();

        this.alertPersistence.create(alert1);
        this.alertPersistence.create(alert2);
        this.alertPersistence.create(alert3);

        List<Alert> result = this.alertPersistence.findByEngagementLetterId(engagementLetterId1);

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Alert::getTitle)
                .containsExactlyInAnyOrder("Alert 1", "Alert 2");
        assertThat(result)
                .extracting(Alert::getEngagementLetterId)
                .containsOnly(engagementLetterId1);
    }

    @Test
    void testFindByEngagementLetterIdEmpty() {
        UUID engagementLetterId = UUID.randomUUID();

        List<Alert> result = this.alertPersistence.findByEngagementLetterId(engagementLetterId);

        assertThat(result).isEmpty();
    }

    @Test
    void testCancelAlertUsingUpdate() {
        UUID alertId = UUID.randomUUID();
        UUID engagementLetterId = UUID.randomUUID();

        LocalDateTime createdAt = LocalDateTime.of(2026, 4, 10, 10, 0);
        LocalDateTime dueDate = LocalDateTime.of(2026, 4, 25, 18, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 4, 13, 12, 30);

        AlertNotification notification1 = AlertNotification.builder()
                .id(UUID.randomUUID())
                .offsetMinutes(-1440)
                .triggerAt(dueDate.plusMinutes(-1440))
                .status(Status.PENDING)
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .build();

        AlertNotification notification2 = AlertNotification.builder()
                .id(UUID.randomUUID())
                .offsetMinutes(-120)
                .triggerAt(dueDate.plusMinutes(-120))
                .status(Status.PENDING)
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .build();

        Alert alert = Alert.builder()
                .id(alertId)
                .title("Original title")
                .description("Original description")
                .dueDate(dueDate)
                .engagementLetterId(engagementLetterId)
                .status(Status.PENDING)
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .createdBy("creator")
                .updatedBy("creator")
                .notifications(List.of(notification1, notification2))
                .build();

        this.alertPersistence.create(alert);

        Alert toCancel = this.alertPersistence.readById(alertId);
        toCancel.setStatus(Status.CANCELLED);
        toCancel.setUpdatedAt(updatedAt);
        toCancel.setUpdatedBy("admin");
        toCancel.setNotifications(
                toCancel.getNotifications().stream()
                        .peek(notification -> {
                            notification.setStatus(Status.CANCELLED);
                            notification.setUpdatedAt(updatedAt);
                        })
                        .toList()
        );

        this.alertPersistence.update(toCancel);

        Alert cancelled = this.alertPersistence.readById(alertId);

        assertThat(cancelled.getId()).isEqualTo(alertId);
        assertThat(cancelled.getStatus()).isEqualTo(Status.CANCELLED);
        assertThat(cancelled.getUpdatedAt()).isEqualTo(updatedAt);
        assertThat(cancelled.getUpdatedBy()).isEqualTo("admin");
        assertThat(cancelled.getNotifications()).hasSize(2);
        assertThat(cancelled.getNotifications())
                .extracting(AlertNotification::getStatus)
                .containsOnly(Status.CANCELLED);
        assertThat(cancelled.getNotifications())
                .extracting(AlertNotification::getUpdatedAt)
                .containsOnly(updatedAt);
    }
}
