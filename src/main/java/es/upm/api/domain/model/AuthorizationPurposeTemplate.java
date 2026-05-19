package es.upm.api.domain.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthorizationPurposeTemplate {
    private UUID id;
    @NotNull
    @NotBlank
    private String purpose;

    public AuthorizationPurposeTemplate ofPurpose() {
        return AuthorizationPurposeTemplate.builder().purpose(this.purpose).build();
    }
}
