package es.upm.api.infrastructure.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.upm.api.domain.model.Alert;
import es.upm.api.domain.model.AlertNotification;
import es.upm.api.domain.model.Status;
import es.upm.api.domain.services.AlertService;
import es.upm.api.infrastructure.dtos.AlertUpdateDto;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class AlertResourceIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AlertService alertService;

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_admin"})
    void shouldUpdateAlert() throws Exception {
        UUID alertId = UUID.randomUUID();
        UUID engagementLetterId = UUID.randomUUID();
        LocalDateTime dueDate = LocalDateTime.of(2026, 4, 25, 18, 0);

        AlertUpdateDto dto = AlertUpdateDto.builder()
                .title("Updated title")
                .description("Updated description")
                .dueDate(dueDate)
                .build();

        AlertNotification notification = AlertNotification.builder()
                .id(UUID.randomUUID())
                .offsetMinutes(-120)
                .triggerAt(dueDate.plusMinutes(-120))
                .status(Status.PENDING)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .build();

        Alert updatedAlert = Alert.builder()
                .id(alertId)
                .title("Updated title")
                .description("Updated description")
                .dueDate(dueDate)
                .engagementLetterId(engagementLetterId)
                .status(Status.PENDING)
                .createdAt(LocalDateTime.now().minusDays(3))
                .updatedAt(LocalDateTime.now())
                .createdBy("creator")
                .updatedBy("admin")
                .notifications(List.of(notification))
                .build();

        BDDMockito.given(this.alertService.update(eq(alertId), any(Alert.class), eq("admin")))
                .willReturn(updatedAlert);

        this.mockMvc.perform(put(AlertResource.ALERTS + "/{alertId}", alertId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(alertId.toString()))
                .andExpect(jsonPath("$.title").value("Updated title"))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.dueDate").value("2026-04-25T18:00:00"))
                .andExpect(jsonPath("$.engagementLetterId").value(engagementLetterId.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.updatedBy").value("admin"))
                .andExpect(jsonPath("$.notifications[0].offsetMinutes").value(-120))
                .andExpect(jsonPath("$.notifications[0].triggerAt").value("2026-04-25T16:00:00"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_admin"})
    void shouldReadAlertById() throws Exception {
        UUID alertId = UUID.randomUUID();
        UUID engagementLetterId = UUID.randomUUID();
        LocalDateTime dueDate = LocalDateTime.of(2026, 4, 25, 18, 0);
        LocalDateTime createdAt = LocalDateTime.of(2026, 4, 10, 9, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 4, 12, 10, 30);

        AlertNotification notification1 = AlertNotification.builder()
                .id(UUID.randomUUID())
                .offsetMinutes(-1440)
                .triggerAt(LocalDateTime.of(2026, 4, 24, 18, 0))
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

        BDDMockito.given(this.alertService.readById(alertId)).willReturn(alert);

        this.mockMvc.perform(get(AlertResource.ALERTS + "/{alertId}", alertId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(alertId.toString()))
                .andExpect(jsonPath("$.title").value("Alert title"))
                .andExpect(jsonPath("$.description").value("Alert description"))
                .andExpect(jsonPath("$.dueDate").value("2026-04-25T18:00:00"))
                .andExpect(jsonPath("$.engagementLetterId").value(engagementLetterId.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.createdAt").value("2026-04-10T09:00:00"))
                .andExpect(jsonPath("$.updatedAt").value("2026-04-12T10:30:00"))
                .andExpect(jsonPath("$.createdBy").value("creator"))
                .andExpect(jsonPath("$.updatedBy").value("updater"))
                .andExpect(jsonPath("$.notifications[0].offsetMinutes").value(-1440))
                .andExpect(jsonPath("$.notifications[0].triggerAt").value("2026-04-24T18:00:00"))
                .andExpect(jsonPath("$.notifications[0].status").value("PENDING"))
                .andExpect(jsonPath("$.notifications[1].offsetMinutes").value(-120))
                .andExpect(jsonPath("$.notifications[1].triggerAt").value("2026-04-25T16:00:00"))
                .andExpect(jsonPath("$.notifications[1].status").value("PENDING"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_admin"})
    void shouldListAlertsByEngagementLetterId() throws Exception {
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

        BDDMockito.given(this.alertService.findByEngagementLetterId(engagementLetterId))
                .willReturn(List.of(alert1, alert2));

        this.mockMvc.perform(get(AlertResource.ALERTS)
                        .param("engagementLetterId", engagementLetterId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(alert1.getId().toString()))
                .andExpect(jsonPath("$[0].title").value("Alert 1"))
                .andExpect(jsonPath("$[0].dueDate").value("2026-04-25T18:00:00"))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[0].notifications").doesNotExist())
                .andExpect(jsonPath("$[1].id").value(alert2.getId().toString()))
                .andExpect(jsonPath("$[1].title").value("Alert 2"))
                .andExpect(jsonPath("$[1].dueDate").value("2026-04-28T10:30:00"))
                .andExpect(jsonPath("$[1].status").value("CANCELLED"))
                .andExpect(jsonPath("$[1].notifications").doesNotExist());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_admin"})
    void shouldReturnEmptyListWhenNoAlertsExist() throws Exception {
        UUID engagementLetterId = UUID.randomUUID();

        BDDMockito.given(this.alertService.findByEngagementLetterId(engagementLetterId))
                .willReturn(List.of());

        this.mockMvc.perform(get(AlertResource.ALERTS)
                        .param("engagementLetterId", engagementLetterId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_admin"})
    void shouldCancelAlert() throws Exception {
        UUID alertId = UUID.randomUUID();
        UUID engagementLetterId = UUID.randomUUID();
        LocalDateTime dueDate = LocalDateTime.of(2026, 4, 25, 18, 0);
        LocalDateTime createdAt = LocalDateTime.of(2026, 4, 10, 9, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 4, 12, 10, 30);

        AlertNotification notification1 = AlertNotification.builder()
                .id(UUID.randomUUID())
                .offsetMinutes(-1440)
                .triggerAt(LocalDateTime.of(2026, 4, 24, 18, 0))
                .status(Status.CANCELLED)
                .shownAt(null)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        AlertNotification notification2 = AlertNotification.builder()
                .id(UUID.randomUUID())
                .offsetMinutes(-120)
                .triggerAt(LocalDateTime.of(2026, 4, 25, 16, 0))
                .status(Status.CANCELLED)
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
                .status(Status.CANCELLED)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .createdBy("creator")
                .updatedBy("admin")
                .notifications(List.of(notification1, notification2))
                .build();

        BDDMockito.given(this.alertService.cancel(alertId, "admin")).willReturn(alert);

        this.mockMvc.perform(patch(AlertResource.ALERTS + "/{alertId}/cancel", alertId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(alertId.toString()))
                .andExpect(jsonPath("$.title").value("Alert title"))
                .andExpect(jsonPath("$.description").value("Alert description"))
                .andExpect(jsonPath("$.dueDate").value("2026-04-25T18:00:00"))
                .andExpect(jsonPath("$.engagementLetterId").value(engagementLetterId.toString()))
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.createdAt").value("2026-04-10T09:00:00"))
                .andExpect(jsonPath("$.updatedAt").value("2026-04-12T10:30:00"))
                .andExpect(jsonPath("$.createdBy").value("creator"))
                .andExpect(jsonPath("$.updatedBy").value("admin"))
                .andExpect(jsonPath("$.notifications[0].status").value("CANCELLED"))
                .andExpect(jsonPath("$.notifications[1].status").value("CANCELLED"));
    }
}