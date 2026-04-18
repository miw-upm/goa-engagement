package es.upm.api.infrastructure.resources;

import es.upm.api.domain.exceptions.BadRequestException;
import es.upm.api.domain.exceptions.NotFoundException;
import es.upm.api.domain.model.Alert;
import es.upm.api.domain.model.AlertNotification;
import es.upm.api.domain.model.PendingAlertNotification;
import es.upm.api.domain.model.Status;
import es.upm.api.domain.services.AlertService;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class AlertNotificationResourceIT {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AlertService alertService;

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_admin"})
    void shouldReturnPendingNotifications() throws Exception {
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

        BDDMockito.given(this.alertService.findPendingNotifications())
                .willReturn(List.of(pendingAlertNotification));

        this.mockMvc.perform(get(AlertNotificationResource.ALERT_NOTIFICATIONS + "/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].notificationId").value(notificationId.toString()))
                .andExpect(jsonPath("$[0].alertId").value(alertId.toString()))
                .andExpect(jsonPath("$[0].offsetMinutes").value(-1440))
                .andExpect(jsonPath("$[0].triggerAt").value("2026-04-24T18:00:00"))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[0].title").value("Alert title"))
                .andExpect(jsonPath("$[0].description").value("Alert description"))
                .andExpect(jsonPath("$[0].dueDate").value("2026-04-25T18:00:00"))
                .andExpect(jsonPath("$[0].engagementLetterId").value(engagementLetterId.toString()));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_admin"})
    void shouldReturnEmptyListWhenNoPendingNotificationsExist() throws Exception {
        BDDMockito.given(this.alertService.findPendingNotifications())
                .willReturn(List.of());

        this.mockMvc.perform(get(AlertNotificationResource.ALERT_NOTIFICATIONS + "/pending"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_admin"})
    void shouldMarkNotificationAsShown() throws Exception {
        UUID notificationId = UUID.randomUUID();

        BDDMockito.willDoNothing().given(this.alertService).markNotificationAsShown(notificationId);

        this.mockMvc.perform(patch(AlertNotificationResource.ALERT_NOTIFICATIONS + "/{notificationId}/shown", notificationId))
                .andExpect(status().isOk());

        verify(this.alertService).markNotificationAsShown(notificationId);
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_admin"})
    void shouldReturnNotFoundWhenNotificationDoesNotExist() throws Exception {
        UUID notificationId = UUID.randomUUID();

        BDDMockito.willThrow(new NotFoundException("The AlertNotification ID doesn't exist: " + notificationId))
                .given(this.alertService).markNotificationAsShown(notificationId);

        this.mockMvc.perform(patch(AlertNotificationResource.ALERT_NOTIFICATIONS + "/{notificationId}/shown", notificationId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_admin"})
    void shouldReturnBadRequestWhenNotificationStatusIsInvalid() throws Exception {
        UUID notificationId = UUID.randomUUID();

        BDDMockito.willThrow(new BadRequestException("Only pending notifications can be marked as shown"))
                .given(this.alertService).markNotificationAsShown(notificationId);

        this.mockMvc.perform(patch(AlertNotificationResource.ALERT_NOTIFICATIONS + "/{notificationId}/shown", notificationId))
                .andExpect(status().isBadRequest());
    }
}
