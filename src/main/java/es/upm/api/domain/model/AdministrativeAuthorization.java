package es.upm.api.domain.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import es.upm.api.domain.model.external.UserSnapshot;
import es.upm.miw.exception.ConflictException;
import es.upm.miw.validations.ListNotEmpty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

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

    public void add(AdministrativeAuthorizationSignature signature) {
        this.signatures = this.signatures == null ? new ArrayList<>() : new ArrayList<>(this.signatures);
        boolean alreadySigned = this.signatures.stream()
                .anyMatch(existing -> signature.getSignerId().equals(existing.getSignerId()));
        if (alreadySigned) {
            throw new ConflictException("El usuario ya firmó esta autorización administrativa: " + signature.getSignerId());
        }
        this.signatures.add(signature);
    }

    public boolean isUserIncluded(UUID userId) {
        return Stream.concat(
                        Optional.ofNullable(this.authorizingCustomers).orElse(List.of()).stream(),
                        Optional.ofNullable(this.authorizedRepresentatives).orElse(List.of()).stream())
                .map(UserSnapshot::getId)
                .anyMatch(userId::equals);
    }
}
