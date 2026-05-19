package es.upm.api.domain.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
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
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdministrativeAuthorization {
    private UUID id;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
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

    public boolean isAuthorizingCustomer(UUID userId) {
        return Optional.ofNullable(this.authorizingCustomers)
                .orElse(List.of())
                .stream()
                .map(UserSnapshot::getId)
                .anyMatch(userId::equals);
    }

    public List<UserSnapshot> findPendingSigners() {
        Set<UUID> signedIds = Optional.ofNullable(this.signatures)
                .orElse(List.of())
                .stream()
                .map(AdministrativeAuthorizationSignature::getSignerId)
                .collect(HashSet::new, Set::add, Set::addAll);
        return Optional.ofNullable(this.authorizingCustomers)
                .orElse(List.of())
                .stream()
                .filter(customer -> !signedIds.contains(customer.getId()))
                .toList();
    }

    public boolean isSigned() {
        return this.findPendingSigners().isEmpty();
    }

    public boolean isClientInAuthorization(List<UUID> clientIds) {
        return Optional.ofNullable(this.authorizingCustomers)
                .orElse(List.of())
                .stream()
                .map(UserSnapshot::getId)
                .anyMatch(clientIds::contains);
    }

    public AdministrativeAuthorization ofSummary() {
        return AdministrativeAuthorization.builder()
                .id(this.id)
                .lastUpdatedDate(this.lastUpdatedDate)
                .authorizingCustomers(this.authorizingCustomers.stream()
                        .map(UserSnapshot::ofSummary)
                        .toList())
                .authorizedRepresentatives(this.authorizedRepresentatives.stream()
                        .map(UserSnapshot::ofSummary)
                        .toList())
                .authorizationPurpose(this.authorizationPurpose)
                .signatures(this.signatures.stream()
                        .map(AdministrativeAuthorizationSignature::ofSummary)
                        .toList())
                .build();
    }

    public String buildDate() {
        return "En Madrid, a " + lastUpdatedDate
                .format(DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Locale.of("es", "ES")));
    }

    public String buildCistomersFullNameIdentity() {
        List<UserSnapshot> clients = new ArrayList<>();
        if (this.authorizingCustomers != null && !this.authorizingCustomers.isEmpty()) {
            clients.addAll(this.authorizingCustomers);
        }

        return clients.stream()
                .map(UserSnapshot::toDonFullNameAndIdentity)
                .collect(Collectors.joining(", "));
    }
    public String buildRepresentativesFullNameIdentity() {
        List<UserSnapshot> clients = new ArrayList<>();
        if (this.authorizedRepresentatives != null && !this.authorizedRepresentatives.isEmpty()) {
            clients.addAll(this.authorizedRepresentatives);
        }

        return clients.stream()
                .map(UserSnapshot::toDonFullNameAndIdentity)
                .collect(Collectors.joining(", "));
    }

}
