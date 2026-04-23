package es.upm.api.infrastructure.mongodb.persistence;

import es.upm.api.domain.model.*;
import es.upm.api.domain.model.snapshots.UserSnapshot;
import es.upm.api.infrastructure.mongodb.repositories.EngagementLetterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@ActiveProfiles("test")
class EngagementLetterPersistenceMongodbIT {

    @Autowired
    private EngagementLetterRepository engagementLetterRepository;

    private EngagementLetterPersistenceMongodb engagementLetterPersistence;

    @BeforeEach
    void setUp() {
        this.engagementLetterPersistence = new EngagementLetterPersistenceMongodb(this.engagementLetterRepository);
        this.engagementLetterRepository.deleteAll();
    }

    @Test
    void testUpdateAcceptanceEngagements() {
        UUID engagementLetterId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID signerId = UUID.randomUUID();
        LocalDate creationDate = LocalDate.of(2026, 4, 1);
        LocalDateTime signatureDate = LocalDateTime.of(2026, 4, 9, 11, 15, 0);

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

        EngagementLetter toUpdate = this.engagementLetterPersistence.readById(engagementLetterId);
        toUpdate.setAcceptanceEngagements(List.of(AcceptanceEngagement.builder()
                .signatureDate(signatureDate)
                .signer(UserSnapshot.builder().id(signerId).build())
                .build()));

        this.engagementLetterPersistence.update(engagementLetterId, toUpdate);

        EngagementLetter updated = this.engagementLetterPersistence.readById(engagementLetterId);
        assertThat(updated.getLastUpdatedDate()).isEqualTo(creationDate);
        assertThat(updated.getAcceptanceEngagements()).hasSize(1);
        assertThat(updated.getAcceptanceEngagements().get(0).getSignatureDate()).isEqualTo(signatureDate);
        assertThat(updated.getAcceptanceEngagements().get(0).getSigner().getId()).isEqualTo(signerId);
    }
}
