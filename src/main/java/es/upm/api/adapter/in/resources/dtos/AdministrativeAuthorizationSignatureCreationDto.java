package es.upm.api.adapter.in.resources.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdministrativeAuthorizationSignatureCreationDto {
    @NotNull
    @NotBlank
    private String signature;
}
