package es.upm.api.infrastructure.mongodb.repositories;

import es.upm.api.domain.model.Status;
import es.upm.api.infrastructure.mongodb.entities.AlertEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@ActiveProfiles("test")
class AlertRepositoryTest {

    @Autowired
    private AlertRepository alertRepository;

    @BeforeEach
    void setUp() {
        this.alertRepository.deleteAll();
    }

    @Test
    void testFindByEngagementLetterId() {
        UUID engagementLetterId1 = UUID.randomUUID();
        UUID engagementLetterId2 = UUID.randomUUID();

        AlertEntity alert1 = AlertEntity.builder()
                .id(UUID.randomUUID())
                .title("Alert 1")
                .dueDate(LocalDateTime.of(2026, 4, 25, 18, 0))
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId1)
                .build();

        AlertEntity alert2 = AlertEntity.builder()
                .id(UUID.randomUUID())
                .title("Alert 2")
                .dueDate(LocalDateTime.of(2026, 4, 28, 10, 30))
                .status(Status.CANCELLED)
                .engagementLetterId(engagementLetterId1)
                .build();

        AlertEntity alert3 = AlertEntity.builder()
                .id(UUID.randomUUID())
                .title("Alert 3")
                .dueDate(LocalDateTime.of(2026, 5, 1, 12, 0))
                .status(Status.PENDING)
                .engagementLetterId(engagementLetterId2)
                .build();

        this.alertRepository.saveAll(List.of(alert1, alert2, alert3));

        List<AlertEntity> result = this.alertRepository.findByEngagementLetterId(engagementLetterId1);

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(AlertEntity::getTitle)
                .containsExactlyInAnyOrder("Alert 1", "Alert 2");
        assertThat(result)
                .extracting(AlertEntity::getEngagementLetterId)
                .containsOnly(engagementLetterId1);
    }

    @Test
    void testFindByEngagementLetterIdEmpty() {
        List<AlertEntity> result = this.alertRepository.findByEngagementLetterId(UUID.randomUUID());
        assertThat(result).isEmpty();
    }
}