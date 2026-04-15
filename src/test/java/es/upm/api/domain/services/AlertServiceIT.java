package es.upm.api.domain.services;

import es.upm.api.domain.exceptions.BadRequestException;
import es.upm.api.domain.exceptions.NotFoundException;
import es.upm.api.domain.model.Alert;
import es.upm.api.domain.model.AlertNotification;
import es.upm.api.domain.model.EngagementLetter;
import es.upm.api.domain.model.Status;
import es.upm.api.domain.persistence.AlertPersistence;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
class AlertServiceIT {

    @Autowired
    private AlertService alertService;

    @MockitoBean
    private AlertPersistence alertPersistence;

    @MockitoBean
    private EngagementLetterService engagementLetterService;

    @Test
    void testCreateSuccessWithDefaultNotificationsWhenNotificationsIsEmpty() {
        UUID engagementLetterId = UUID.randomUUID();
        LocalDateTime dueDate = LocalDateTime.of(2026, 4, 25, 18, 0);

        Alert alert = Alert.builder()
                .title("Alert title")
                .description("Alert description")
                .dueDate(dueDate)
                .engagementLetterId(engagementLetterId)
                .notifications(List.of())
                .build();

        BDDMockito.given(this.engagementLetterService.readById(engagementLetterId))
                .willReturn(new EngagementLetter());
        BDDMockito.willDoNothing().given(this.alertPersistence).create(any(Alert.class));

        Alert createdAlert = this.alertService.create(alert, "admin");

        assertThat(createdAlert).isNotNull();
        assertThat(createdAlert.getId()).isNotNull();
        assertThat(createdAlert.getStatus()).isEqualTo(Status.PENDING);
        assertThat(createdAlert.getCreatedAt()).isNotNull();
        assertThat(createdAlert.getUpdatedAt()).isNotNull();
        assertThat(createdAlert.getCreatedBy()).isEqualTo("admin");
        assertThat(createdAlert.getUpdatedBy()).isEqualTo("admin");
        assertThat(createdAlert.getNotifications()).hasSize(3);
        assertThat(createdAlert.getNotifications())
                .extracting(AlertNotification::getOffsetMinutes)
                .containsExactly(-4320, -1440, -120);
        assertThat(createdAlert.getNotifications())
                .extracting(AlertNotification::getTriggerAt)
                .containsExactly(
                        dueDate.plusMinutes(-4320),
                        dueDate.plusMinutes(-1440),
                        dueDate.plusMinutes(-120)
                );
        assertThat(createdAlert.getNotifications())
                .extracting(AlertNotification::getStatus)
                .containsOnly(Status.PENDING);

        ArgumentCaptor<Alert> captor = ArgumentCaptor.forClass(Alert.class);
        verify(this.engagementLetterService).readById(engagementLetterId);
        verify(this.alertPersistence).create(captor.capture());

        Alert persistedAlert = captor.getValue();
        assertThat(persistedAlert).isNotNull();
        assertThat(persistedAlert.getId()).isNotNull();
        assertThat(persistedAlert.getStatus()).isEqualTo(Status.PENDING);
        assertThat(persistedAlert.getCreatedAt()).isNotNull();
        assertThat(persistedAlert.getUpdatedAt()).isNotNull();
        assertThat(persistedAlert.getCreatedBy()).isEqualTo("admin");
        assertThat(persistedAlert.getUpdatedBy()).isEqualTo("admin");
        assertThat(persistedAlert.getNotifications()).hasSize(3);
        assertThat(persistedAlert.getNotifications())
                .extracting(AlertNotification::getOffsetMinutes)
                .containsExactly(-4320, -1440, -120);
        assertThat(persistedAlert.getNotifications())
                .extracting(AlertNotification::getTriggerAt)
                .containsExactly(
                        dueDate.plusMinutes(-4320),
                        dueDate.plusMinutes(-1440),
                        dueDate.plusMinutes(-120)
                );
        assertThat(persistedAlert.getNotifications())
                .extracting(AlertNotification::getStatus)
                .containsOnly(Status.PENDING);
    }

    @Test
    void testCreateSuccessWithDefaultNotificationsWhenNotificationsIsNull() {
        UUID engagementLetterId = UUID.randomUUID();
        LocalDateTime dueDate = LocalDateTime.of(2026, 4, 26, 10, 30);

        Alert alert = Alert.builder()
                .title("Alert title")
                .description("Alert description")
                .dueDate(dueDate)
                .engagementLetterId(engagementLetterId)
                .notifications(null)
                .build();

        BDDMockito.given(this.engagementLetterService.readById(engagementLetterId))
                .willReturn(new EngagementLetter());
        BDDMockito.willDoNothing().given(this.alertPersistence).create(any(Alert.class));

        Alert createdAlert = this.alertService.create(alert, "manager");

        assertThat(createdAlert).isNotNull();
        assertThat(createdAlert.getId()).isNotNull();
        assertThat(createdAlert.getStatus()).isEqualTo(Status.PENDING);
        assertThat(createdAlert.getCreatedAt()).isNotNull();
        assertThat(createdAlert.getUpdatedAt()).isNotNull();
        assertThat(createdAlert.getCreatedBy()).isEqualTo("manager");
        assertThat(createdAlert.getUpdatedBy()).isEqualTo("manager");
        assertThat(createdAlert.getNotifications()).hasSize(3);
        assertThat(createdAlert.getNotifications())
                .extracting(AlertNotification::getOffsetMinutes)
                .containsExactly(-4320, -1440, -120);
        assertThat(createdAlert.getNotifications())
                .extracting(AlertNotification::getTriggerAt)
                .containsExactly(
                        dueDate.plusMinutes(-4320),
                        dueDate.plusMinutes(-1440),
                        dueDate.plusMinutes(-120)
                );
        assertThat(createdAlert.getNotifications())
                .extracting(AlertNotification::getStatus)
                .containsOnly(Status.PENDING);

        ArgumentCaptor<Alert> captor = ArgumentCaptor.forClass(Alert.class);
        verify(this.engagementLetterService).readById(engagementLetterId);
        verify(this.alertPersistence).create(captor.capture());

        Alert persistedAlert = captor.getValue();
        assertThat(persistedAlert).isNotNull();
        assertThat(persistedAlert.getId()).isNotNull();
        assertThat(persistedAlert.getStatus()).isEqualTo(Status.PENDING);
        assertThat(persistedAlert.getCreatedAt()).isNotNull();
        assertThat(persistedAlert.getUpdatedAt()).isNotNull();
        assertThat(persistedAlert.getCreatedBy()).isEqualTo("manager");
        assertThat(persistedAlert.getUpdatedBy()).isEqualTo("manager");
        assertThat(persistedAlert.getNotifications()).hasSize(3);
        assertThat(persistedAlert.getNotifications())
                .extracting(AlertNotification::getOffsetMinutes)
                .containsExactly(-4320, -1440, -120);
        assertThat(persistedAlert.getNotifications())
                .extracting(AlertNotification::getTriggerAt)
                .containsExactly(
                        dueDate.plusMinutes(-4320),
                        dueDate.plusMinutes(-1440),
                        dueDate.plusMinutes(-120)
                );
        assertThat(persistedAlert.getNotifications())
                .extracting(AlertNotification::getStatus)
                .containsOnly(Status.PENDING);
    }

    @Test
    void testUpdateSuccessWhenDueDateChanges() {
        UUID alertId = UUID.randomUUID();
        LocalDateTime oldDueDate = LocalDateTime.of(2026, 4, 20, 10, 0);
        LocalDateTime newDueDate = LocalDateTime.of(2026, 4, 25, 18, 0);

        AlertNotification notification1 = AlertNotification.builder()
                .id(UUID.randomUUID())
                .offsetMinutes(-1440)
                .triggerAt(oldDueDate.plusMinutes(-1440))
                .status(Status.PENDING)
                .createdAt(LocalDateTime.now().minusDays(2))
                .updatedAt(LocalDateTime.now().minusDays(2))
                .build();

        AlertNotification notification2 = AlertNotification.builder()
                .id(UUID.randomUUID())
                .offsetMinutes(-120)
                .triggerAt(oldDueDate.plusMinutes(-120))
                .status(Status.PENDING)
                .createdAt(LocalDateTime.now().minusDays(2))
                .updatedAt(LocalDateTime.now().minusDays(2))
                .build();

        Alert existingAlert = Alert.builder()
                .id(alertId)
                .title("Old title")
                .description("Old description")
                .dueDate(oldDueDate)
                .engagementLetterId(UUID.randomUUID())
                .status(Status.PENDING)
                .createdAt(LocalDateTime.now().minusDays(5))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .createdBy("creator")
                .updatedBy("creator")
                .notifications(List.of(notification1, notification2))
                .build();

        Alert alertRequest = Alert.builder()
                .title("New title")
                .description("New description")
                .dueDate(newDueDate)
                .build();

        BDDMockito.given(this.alertPersistence.readById(alertId)).willReturn(existingAlert);
        BDDMockito.willDoNothing().given(this.alertPersistence).update(any(Alert.class));

        Alert updatedAlert = this.alertService.update(alertId, alertRequest, "admin");

        assertThat(updatedAlert).isNotNull();
        assertThat(updatedAlert.getTitle()).isEqualTo("New title");
        assertThat(updatedAlert.getDescription()).isEqualTo("New description");
        assertThat(updatedAlert.getDueDate()).isEqualTo(newDueDate);
        assertThat(updatedAlert.getUpdatedBy()).isEqualTo("admin");
        assertThat(updatedAlert.getUpdatedAt()).isNotNull();
        assertThat(updatedAlert.getNotifications()).hasSize(2);
        assertThat(updatedAlert.getNotifications().get(0).getTriggerAt())
                .isEqualTo(newDueDate.plusMinutes(-1440));
        assertThat(updatedAlert.getNotifications().get(1).getTriggerAt())
                .isEqualTo(newDueDate.plusMinutes(-120));

        ArgumentCaptor<Alert> captor = ArgumentCaptor.forClass(Alert.class);
        verify(this.alertPersistence).update(captor.capture());

        Alert persistedAlert = captor.getValue();
        assertThat(persistedAlert.getTitle()).isEqualTo("New title");
        assertThat(persistedAlert.getDescription()).isEqualTo("New description");
        assertThat(persistedAlert.getDueDate()).isEqualTo(newDueDate);
        assertThat(persistedAlert.getUpdatedBy()).isEqualTo("admin");
        assertThat(persistedAlert.getNotifications().get(0).getTriggerAt())
                .isEqualTo(newDueDate.plusMinutes(-1440));
        assertThat(persistedAlert.getNotifications().get(1).getTriggerAt())
                .isEqualTo(newDueDate.plusMinutes(-120));
    }

    @Test
    void testUpdateSuccessWhenDueDateDoesNotChange() {
        UUID alertId = UUID.randomUUID();
        LocalDateTime dueDate = LocalDateTime.of(2026, 4, 20, 10, 0);
        LocalDateTime originalTriggerAt = dueDate.plusMinutes(-120);

        AlertNotification notification = AlertNotification.builder()
                .id(UUID.randomUUID())
                .offsetMinutes(-120)
                .triggerAt(originalTriggerAt)
                .status(Status.PENDING)
                .createdAt(LocalDateTime.now().minusDays(2))
                .updatedAt(LocalDateTime.now().minusDays(2))
                .build();

        Alert existingAlert = Alert.builder()
                .id(alertId)
                .title("Old title")
                .description("Old description")
                .dueDate(dueDate)
                .engagementLetterId(UUID.randomUUID())
                .status(Status.PENDING)
                .createdAt(LocalDateTime.now().minusDays(5))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .createdBy("creator")
                .updatedBy("creator")
                .notifications(List.of(notification))
                .build();

        Alert alertRequest = Alert.builder()
                .title("Updated title")
                .description("Updated description")
                .dueDate(dueDate)
                .build();

        BDDMockito.given(this.alertPersistence.readById(alertId)).willReturn(existingAlert);
        BDDMockito.willDoNothing().given(this.alertPersistence).update(any(Alert.class));

        Alert updatedAlert = this.alertService.update(alertId, alertRequest, "manager");

        assertThat(updatedAlert.getTitle()).isEqualTo("Updated title");
        assertThat(updatedAlert.getDescription()).isEqualTo("Updated description");
        assertThat(updatedAlert.getDueDate()).isEqualTo(dueDate);
        assertThat(updatedAlert.getUpdatedBy()).isEqualTo("manager");
        assertThat(updatedAlert.getNotifications().getFirst().getTriggerAt()).isEqualTo(originalTriggerAt);
    }

    @Test
    void testUpdateWhenAlertIsCancelled() {
        UUID alertId = UUID.randomUUID();

        Alert existingAlert = Alert.builder()
                .id(alertId)
                .title("Old title")
                .description("Old description")
                .dueDate(LocalDateTime.now().plusDays(3))
                .engagementLetterId(UUID.randomUUID())
                .status(Status.CANCELLED)
                .createdAt(LocalDateTime.now().minusDays(5))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .createdBy("creator")
                .updatedBy("creator")
                .build();

        Alert alertRequest = Alert.builder()
                .title("New title")
                .description("New description")
                .dueDate(LocalDateTime.now().plusDays(10))
                .build();

        BDDMockito.given(this.alertPersistence.readById(alertId)).willReturn(existingAlert);

        assertThatThrownBy(() -> this.alertService.update(alertId, alertRequest, "admin"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cancelled alerts cannot be edited");
    }

    @Test
    void testUpdateWhenAlertDoesNotExist() {
        UUID alertId = UUID.randomUUID();

        Alert alertRequest = Alert.builder()
                .title("New title")
                .description("New description")
                .dueDate(LocalDateTime.now().plusDays(10))
                .build();

        BDDMockito.given(this.alertPersistence.readById(alertId))
                .willThrow(new NotFoundException("The Alert ID doesn't exist: " + alertId));

        assertThatThrownBy(() -> this.alertService.update(alertId, alertRequest, "admin"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(alertId.toString());
    }

    @Test
    void testReadByIdSuccess() {
        UUID alertId = UUID.randomUUID();
        UUID engagementLetterId = UUID.randomUUID();
        LocalDateTime dueDate = LocalDateTime.of(2026, 4, 25, 18, 0);
        LocalDateTime now = LocalDateTime.now();

        AlertNotification notification = AlertNotification.builder()
                .id(UUID.randomUUID())
                .offsetMinutes(-120)
                .triggerAt(dueDate.plusMinutes(-120))
                .status(Status.PENDING)
                .shownAt(null)
                .createdAt(now.minusDays(1))
                .updatedAt(now)
                .build();

        Alert alert = Alert.builder()
                .id(alertId)
                .title("Alert title")
                .description("Alert description")
                .dueDate(dueDate)
                .engagementLetterId(engagementLetterId)
                .status(Status.PENDING)
                .createdAt(now.minusDays(2))
                .updatedAt(now)
                .createdBy("creator")
                .updatedBy("creator")
                .notifications(List.of(notification))
                .build();

        BDDMockito.given(this.alertPersistence.readById(alertId)).willReturn(alert);

        Alert result = this.alertService.readById(alertId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(alertId);
        assertThat(result.getTitle()).isEqualTo("Alert title");
        assertThat(result.getDescription()).isEqualTo("Alert description");
        assertThat(result.getDueDate()).isEqualTo(dueDate);
        assertThat(result.getEngagementLetterId()).isEqualTo(engagementLetterId);
        assertThat(result.getStatus()).isEqualTo(Status.PENDING);
        assertThat(result.getCreatedBy()).isEqualTo("creator");
        assertThat(result.getUpdatedBy()).isEqualTo("creator");
        assertThat(result.getNotifications()).hasSize(1);
        assertThat(result.getNotifications().getFirst().getOffsetMinutes()).isEqualTo(-120);
        assertThat(result.getNotifications().getFirst().getTriggerAt()).isEqualTo(dueDate.plusMinutes(-120));
    }

    @Test
    void testReadByIdWhenAlertDoesNotExist() {
        UUID alertId = UUID.randomUUID();

        BDDMockito.given(this.alertPersistence.readById(alertId))
                .willThrow(new NotFoundException("The Alert ID doesn't exist: " + alertId));

        assertThatThrownBy(() -> this.alertService.readById(alertId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(alertId.toString());
    }

    @Test
    void testFindByEngagementLetterIdSuccess() {
        UUID engagementLetterId = UUID.randomUUID();

        Alert alert1 = Alert.builder()
                .id(UUID.randomUUID())
                .title("Alert 1")
                .dueDate(LocalDateTime.of(2026, 4, 25, 18, 0))
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId)
                .build();

        Alert alert2 = Alert.builder()
                .id(UUID.randomUUID())
                .title("Alert 2")
                .dueDate(LocalDateTime.of(2026, 4, 28, 10, 30))
                .status(Status.CANCELLED)
                .engagementLetterId(engagementLetterId)
                .build();

        BDDMockito.given(this.engagementLetterService.readById(engagementLetterId))
                .willReturn(null);
        BDDMockito.given(this.alertPersistence.findByEngagementLetterId(engagementLetterId))
                .willReturn(List.of(alert1, alert2));

        List<Alert> result = this.alertService.findByEngagementLetterId(engagementLetterId);

        assertThat(result).hasSize(2);
        assertThat(result.getFirst().getTitle()).isEqualTo("Alert 1");
        assertThat(result.getFirst().getDueDate()).isEqualTo(LocalDateTime.of(2026, 4, 25, 18, 0));
        assertThat(result.getFirst().getStatus()).isEqualTo(Status.PENDING);

        assertThat(result.get(1).getTitle()).isEqualTo("Alert 2");
        assertThat(result.get(1).getDueDate()).isEqualTo(LocalDateTime.of(2026, 4, 28, 10, 30));
        assertThat(result.get(1).getStatus()).isEqualTo(Status.CANCELLED);
    }

    @Test
    void testFindByEngagementLetterIdEmptyList() {
        UUID engagementLetterId = UUID.randomUUID();

        BDDMockito.given(this.engagementLetterService.readById(engagementLetterId))
                .willReturn(null);
        BDDMockito.given(this.alertPersistence.findByEngagementLetterId(engagementLetterId))
                .willReturn(List.of());

        List<Alert> result = this.alertService.findByEngagementLetterId(engagementLetterId);

        assertThat(result).isEmpty();
    }

    @Test
    void testFindByEngagementLetterIdWhenEngagementLetterDoesNotExist() {
        UUID engagementLetterId = UUID.randomUUID();

        BDDMockito.given(this.engagementLetterService.readById(engagementLetterId))
                .willThrow(new NotFoundException("The EngagementLetter ID doesn't exist: " + engagementLetterId));

        assertThatThrownBy(() -> this.alertService.findByEngagementLetterId(engagementLetterId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(engagementLetterId.toString());
    }

    @Test
    void testCancelSuccess() {
        UUID alertId = UUID.randomUUID();
        LocalDateTime dueDate = LocalDateTime.of(2026, 4, 25, 18, 0);

        AlertNotification notification1 = AlertNotification.builder()
                .id(UUID.randomUUID())
                .offsetMinutes(-1440)
                .triggerAt(dueDate.plusMinutes(-1440))
                .status(Status.PENDING)
                .createdAt(LocalDateTime.now().minusDays(2))
                .updatedAt(LocalDateTime.now().minusDays(2))
                .build();

        AlertNotification notification2 = AlertNotification.builder()
                .id(UUID.randomUUID())
                .offsetMinutes(-120)
                .triggerAt(dueDate.plusMinutes(-120))
                .status(Status.PENDING)
                .createdAt(LocalDateTime.now().minusDays(2))
                .updatedAt(LocalDateTime.now().minusDays(2))
                .build();

        Alert existingAlert = Alert.builder()
                .id(alertId)
                .title("Alert title")
                .description("Alert description")
                .dueDate(dueDate)
                .engagementLetterId(UUID.randomUUID())
                .status(Status.PENDING)
                .createdAt(LocalDateTime.now().minusDays(5))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .createdBy("creator")
                .updatedBy("creator")
                .notifications(List.of(notification1, notification2))
                .build();

        BDDMockito.given(this.alertPersistence.readById(alertId)).willReturn(existingAlert);
        BDDMockito.willDoNothing().given(this.alertPersistence).update(any(Alert.class));

        Alert cancelledAlert = this.alertService.cancel(alertId, "admin");

        assertThat(cancelledAlert).isNotNull();
        assertThat(cancelledAlert.getStatus()).isEqualTo(Status.CANCELLED);
        assertThat(cancelledAlert.getUpdatedBy()).isEqualTo("admin");
        assertThat(cancelledAlert.getUpdatedAt()).isNotNull();
        assertThat(cancelledAlert.getNotifications()).hasSize(2);
        assertThat(cancelledAlert.getNotifications())
                .extracting(AlertNotification::getStatus)
                .containsOnly(Status.CANCELLED);
        assertThat(cancelledAlert.getNotifications())
                .extracting(AlertNotification::getUpdatedAt)
                .doesNotContainNull();

        ArgumentCaptor<Alert> captor = ArgumentCaptor.forClass(Alert.class);
        verify(this.alertPersistence).update(captor.capture());

        Alert persistedAlert = captor.getValue();
        assertThat(persistedAlert.getStatus()).isEqualTo(Status.CANCELLED);
        assertThat(persistedAlert.getUpdatedBy()).isEqualTo("admin");
        assertThat(persistedAlert.getNotifications())
                .extracting(AlertNotification::getStatus)
                .containsOnly(Status.CANCELLED);
    }

    @Test
    void testCancelWhenAlreadyCancelled() {
        UUID alertId = UUID.randomUUID();
        LocalDateTime dueDate = LocalDateTime.of(2026, 4, 25, 18, 0);

        AlertNotification notification = AlertNotification.builder()
                .id(UUID.randomUUID())
                .offsetMinutes(-120)
                .triggerAt(dueDate.plusMinutes(-120))
                .status(Status.CANCELLED)
                .createdAt(LocalDateTime.now().minusDays(2))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build();

        Alert existingAlert = Alert.builder()
                .id(alertId)
                .title("Alert title")
                .description("Alert description")
                .dueDate(dueDate)
                .engagementLetterId(UUID.randomUUID())
                .status(Status.CANCELLED)
                .createdAt(LocalDateTime.now().minusDays(5))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .createdBy("creator")
                .updatedBy("creator")
                .notifications(List.of(notification))
                .build();

        BDDMockito.given(this.alertPersistence.readById(alertId)).willReturn(existingAlert);
        BDDMockito.willDoNothing().given(this.alertPersistence).update(any(Alert.class));

        Alert cancelledAlert = this.alertService.cancel(alertId, "admin");

        assertThat(cancelledAlert.getStatus()).isEqualTo(Status.CANCELLED);
        assertThat(cancelledAlert.getUpdatedBy()).isEqualTo("admin");
        assertThat(cancelledAlert.getNotifications()).hasSize(1);
        assertThat(cancelledAlert.getNotifications().getFirst().getStatus()).isEqualTo(Status.CANCELLED);
    }

    @Test
    void testCancelWhenAlertDoesNotExist() {
        UUID alertId = UUID.randomUUID();

        BDDMockito.given(this.alertPersistence.readById(alertId))
                .willThrow(new NotFoundException("The Alert ID doesn't exist: " + alertId));

        assertThatThrownBy(() -> this.alertService.cancel(alertId, "admin"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(alertId.toString());
    }
}