package es.upm.api.infrastructure.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertNotificationConfigDto {
    @NotNull(message = "Offset minutes list is required")
    private List<Integer> offsetMinutes;
}
