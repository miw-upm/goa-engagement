package es.upm.api.adapter.in.resources;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AcceptanceEngagementCreationDto {
    private Boolean documentAccepted;
    private String signature;
}
