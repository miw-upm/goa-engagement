package es.upm.api.infrastructure.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicEngagementLetterAcceptRequestDto {
    @NotBlank(message = "Public access token is required")
    private String token;
}
