package es.upm.api.domain.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import es.upm.api.domain.model.validations.ListNotEmpty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EngagementLetter {
    private UUID id;
    private Boolean budgetOnly;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate creationDate;
    @Min(0)
    @Max(100)
    private Integer discount;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate closingDate;
    @NotNull
    private UserDto owner;
    private List<UserDto> attachments;
    @ListNotEmpty
    private List<LegalProcedure> legalProcedures;
    @ListNotEmpty
    private List<PaymentMethod> paymentMethods;
    private String legalClause;
    private List<AcceptanceEngagement> acceptanceEngagements;

    public String buildClientsFullNameIdentity() {
        List<UserDto> clients = new ArrayList<>();
        clients.add(this.owner);
        if (this.attachments != null && !this.attachments.isEmpty()) {
            clients.addAll(this.attachments);
        }

        return clients.stream()
                .map(UserDto::toFullNameAndIdentity)
                .collect(Collectors.joining(", "));
    }

    public List<String> buildClientsName() {
        List<String> names = new ArrayList<>();
        names.add(this.getOwner().toFullName());
        if (this.getAttachments() != null) {
            this.getAttachments().forEach(user -> names.add(user.toFullName()));
        }
        return names;
    }

    public String buildCreationDate() {
        return "En Madrid, a " + creationDate
                .format(DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Locale.of("es", "ES")));
    }
}
