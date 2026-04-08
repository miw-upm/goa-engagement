package es.upm.api.infrastructure.resources;

import es.upm.api.domain.model.EngagementLetter;
import es.upm.api.domain.model.LegalProcedure;
import es.upm.api.domain.model.PaymentMethod;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
public class PublicEngagementLetterResponse {
    private final UUID id;
    private final LocalDate creationDate;
    private final Integer discount;
    private final LocalDate closingDate;
    private final List<LegalProcedure> legalProcedures;
    private final List<PaymentMethod> paymentMethods;

    public PublicEngagementLetterResponse(EngagementLetter engagementLetter) {
        this.id = engagementLetter.getId();
        this.creationDate = engagementLetter.getCreationDate();
        this.discount = engagementLetter.getDiscount();
        this.closingDate = engagementLetter.getClosingDate();
        this.legalProcedures = engagementLetter.getLegalProcedures();
        this.paymentMethods = engagementLetter.getPaymentMethods();
    }
}
