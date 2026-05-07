package es.upm.api.domain.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import es.upm.api.domain.model.external.UserSnapshot;
import es.upm.miw.validations.ListNotEmpty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdministrativeAuthorization {
    private UUID id;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate lastUpdatedDate;
    @ListNotEmpty
    private List<UserSnapshot> authorizingCustomers;
    @ListNotEmpty
    private List<UserSnapshot> authorizedRepresentatives;
    @NotNull
    @NotBlank
    private String authorizationPurpose;
    private List<AdministrativeAuthorizationSignature> signatures;
}
