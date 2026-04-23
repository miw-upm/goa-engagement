package es.upm.api.adapter.out.legal.mongo.engagementletter;

import es.upm.api.domain.model.EngagementLetter;
import es.upm.api.domain.model.LegalProcedure;
import es.upm.api.domain.model.PaymentMethod;
import es.upm.api.domain.model.external.UserSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@ActiveProfiles("test")
class EngagementLetterAdapterIT {

    @Autowired
    private EngagementLetterRepository engagementLetterRepository;

    private EngagementLetterAdapter engagementLetterPersistence;

    @BeforeEach
    void setUp() {
        this.engagementLetterPersistence = new EngagementLetterAdapter(this.engagementLetterRepository);
        this.engagementLetterRepository.deleteAll();
    }

    @Test
    void testUpdateAcceptanceEngagements() {
        UUID engagementLetterId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        LocalDate creationDate = LocalDate.of(2026, 4, 1);

        EngagementLetter engagementLetter = EngagementLetter.builder()
                .id(engagementLetterId)
                .lastUpdatedDate(creationDate)
                .discount(10)
                .owner(UserSnapshot.builder().id(ownerId).build())
                .legalProcedures(List.of(LegalProcedure.builder()
                        .title("procedimiento")
                        .budget(BigDecimal.TEN)
                        .legalTasks(List.of("task"))
                        .build()))
                .paymentMethods(List.of(PaymentMethod.builder()
                        .description("Todo")
                        .percentage("100%")
                        .build()))
                .build();
        this.engagementLetterPersistence.create(engagementLetter);

        EngagementLetter updated = this.engagementLetterPersistence.readById(engagementLetterId);
        assertThat(updated.getLastUpdatedDate()).isEqualTo(creationDate);
        assertThat(updated.getAcceptanceEngagements()).hasSize(1);

    }
}
