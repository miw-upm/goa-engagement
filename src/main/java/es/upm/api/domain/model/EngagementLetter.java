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
import java.util.List;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EngagementLetter {
    private UUID id;
    @Min(0)
    @Max(100)
    private Integer discount;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate creationDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate closingDate;
    @NotNull
    private UserDto owner;
    private List<UserDto> attachments;
    @ListNotEmpty
    private List<LegalProcedure> legalProcedures;
    private List<AcceptanceDocument> acceptanceDocuments;
    private List<PaymentMethod> paymentMethods;
}
