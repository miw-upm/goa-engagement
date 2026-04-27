package es.upm.api.domain.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import es.upm.api.domain.model.external.UserSnapshot;
import es.upm.miw.exception.ConflictException;
import es.upm.miw.validations.ListNotEmpty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
public class EngagementLetter {
    private UUID id;
    private Boolean budgetOnly;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate lastUpdatedDate;
    @Min(0)
    @Max(100)
    private Integer discount;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate closingDate;
    @NotNull
    private UserSnapshot owner;
    private List<UserSnapshot> attachments;
    @ListNotEmpty
    private List<LegalProcedure> legalProcedures;
    @ListNotEmpty
    private List<PaymentMethod> paymentMethods;
    private String legalClause;
    private List<AcceptanceEngagement> acceptanceEngagements;

    public String buildClientsFullNameIdentity() {
        List<UserSnapshot> clients = new ArrayList<>();
        clients.add(this.owner);
        if (this.attachments != null && !this.attachments.isEmpty()) {
            clients.addAll(this.attachments);
        }

        return clients.stream()
                .map(UserSnapshot::toDonFullNameAndIdentity)
                .collect(Collectors.joining(", "));
    }

    public List<String> buildClientsName() {
        List<String> names = new ArrayList<>();
        names.add(this.getOwner().toFullName());
        if (this.getAttachments() != null) {
            this.getAttachments().forEach(user -> names.add(user.toDonFullName()));
        }
        return names;
    }

    public String buildCreationDate() {
        return "En Madrid, a " + lastUpdatedDate
                .format(DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Locale.of("es", "ES")));
    }

    public void add(AcceptanceEngagement acceptance) {
        this.acceptanceEngagements = this.acceptanceEngagements == null
                ? new ArrayList<>()
                : new ArrayList<>(this.acceptanceEngagements);
        boolean mobileInUse = this.getAcceptanceEngagements().stream()
                .anyMatch(signer -> acceptance.getMobile().equals(signer.getMobile()));
        if (mobileInUse) {
            throw new ConflictException("El usuario ya firmó: " + acceptance.getMobile());
        }
        this.acceptanceEngagements.add(acceptance);
    }

    public List<UserSnapshot> findPendingSigners() {
        Set<UUID> signedIds = Optional.ofNullable(this.getAcceptanceEngagements())
                .orElse(List.of()).stream()
                .map(AcceptanceEngagement::getSignerId)
                .collect(Collectors.toSet());

        return Stream.concat(
                        Stream.of(this.getOwner()), Optional.ofNullable(this.getAttachments()).orElse(List.of()).stream())
                .filter(user -> !signedIds.contains(user.getId()))
                .toList();
    }

    public boolean isSigned() {
        return this.findPendingSigners().isEmpty();
    }

    public boolean areAllUsersComplete() {
        return Stream.concat(
                        Stream.of(this.getOwner()), Optional.ofNullable(this.getAttachments()).orElse(List.of()).stream())
                .allMatch(UserSnapshot::isComplete);
    }

    public boolean isClientInLetter(List<UUID> clientIds) {
        return Stream.concat(
                        Stream.of(this.getOwner()),
                        Optional.ofNullable(this.getAttachments()).orElse(List.of()).stream())
                .map(UserSnapshot::getId)
                .anyMatch(clientIds::contains);
    }
}
