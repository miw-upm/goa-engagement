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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
}