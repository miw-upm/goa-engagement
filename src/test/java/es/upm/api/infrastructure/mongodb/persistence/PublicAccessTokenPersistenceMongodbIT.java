package es.upm.api.infrastructure.mongodb.persistence;

import es.upm.api.domain.exceptions.NotFoundException;
import es.upm.api.domain.model.PublicAccessToken;
import es.upm.api.domain.model.TokenPurpose;
import es.upm.api.infrastructure.mongodb.entities.PublicAccessTokenEntity;
import es.upm.api.infrastructure.mongodb.repositories.PublicAccessTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataMongoTest
@ActiveProfiles("test")
class PublicAccessTokenPersistenceMongodbIT {

    @Autowired
    private PublicAccessTokenRepository publicAccessTokenRepository;

    private PublicAccessTokenPersistenceMongodb publicAccessTokenPersistence;

    @BeforeEach
    void setUp() {
        this.publicAccessTokenPersistence = new PublicAccessTokenPersistenceMongodb(this.publicAccessTokenRepository);
        this.publicAccessTokenRepository.deleteAll();
    }

    @Test
    void testCreatePublicAccessToken() {
        UUID id = UUID.randomUUID();
        UUID engagementLetterId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        PublicAccessToken publicAccessToken = PublicAccessToken.builder()
                .id(id)
                .token("public-token-create")
                .purpose(TokenPurpose.ACCEPT_ENGAGEMENT)
                .expiresAt(LocalDateTime.now().plusDays(5))
                .maxUses(5)
                .usedCount(0)
                .isActive(true)
                .engagementLetterId(engagementLetterId)
                .customerId(customerId)
                .build();

        PublicAccessToken created = this.publicAccessTokenPersistence.create(publicAccessToken);

        assertThat(created).isNotNull();
        assertThat(created.getId()).isEqualTo(id);
        assertThat(created.getToken()).isEqualTo("public-token-create");
        assertThat(created.getPurpose()).isEqualTo(TokenPurpose.ACCEPT_ENGAGEMENT);
        assertThat(created.getEngagementLetterId()).isEqualTo(engagementLetterId);
        assertThat(created.getCustomerId()).isEqualTo(customerId);

        Optional<PublicAccessTokenEntity> stored = this.publicAccessTokenRepository.findById(id);
        assertThat(stored).isPresent();
        assertThat(stored.get().getToken()).isEqualTo("public-token-create");
        assertThat(stored.get().getUsedCount()).isZero();
        assertThat(stored.get().getIsActive()).isTrue();
    }

    @Test
    void testReadByToken() {
        UUID id = UUID.randomUUID();
        this.publicAccessTokenRepository.save(PublicAccessTokenEntity.builder()
                .id(id)
                .token("public-token-read")
                .purpose(TokenPurpose.ACCEPT_ENGAGEMENT)
                .expiresAt(LocalDateTime.now().plusDays(2))
                .maxUses(5)
                .usedCount(1)
                .isActive(true)
                .engagementLetterId(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .build());

        PublicAccessToken read = this.publicAccessTokenPersistence.readByToken("public-token-read");

        assertThat(read).isNotNull();
        assertThat(read.getId()).isEqualTo(id);
        assertThat(read.getToken()).isEqualTo("public-token-read");
        assertThat(read.getUsedCount()).isEqualTo(1);
    }

    @Test
    void testReadByTokenWhenTokenDoesNotExist() {
        assertThatThrownBy(() -> this.publicAccessTokenPersistence.readByToken("missing-token"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("missing-token");
    }

    @Test
    void testUpdatePublicAccessToken() {
        UUID id = UUID.randomUUID();
        UUID engagementLetterId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        this.publicAccessTokenRepository.save(PublicAccessTokenEntity.builder()
                .id(id)
                .token("public-token-update")
                .purpose(TokenPurpose.ACCEPT_ENGAGEMENT)
                .expiresAt(LocalDateTime.now().plusDays(3))
                .maxUses(5)
                .usedCount(2)
                .isActive(true)
                .engagementLetterId(engagementLetterId)
                .customerId(customerId)
                .build());

        PublicAccessToken toUpdate = PublicAccessToken.builder()
                .id(id)
                .token("public-token-update")
                .purpose(TokenPurpose.ACCEPT_ENGAGEMENT)
                .expiresAt(LocalDateTime.now().plusDays(3))
                .maxUses(5)
                .usedCount(3)
                .isActive(true)
                .engagementLetterId(engagementLetterId)
                .customerId(customerId)
                .build();

        PublicAccessToken updated = this.publicAccessTokenPersistence.update(toUpdate);

        assertThat(updated.getUsedCount()).isEqualTo(3);
        Optional<PublicAccessTokenEntity> stored = this.publicAccessTokenRepository.findById(id);
        assertThat(stored).isPresent();
        assertThat(stored.get().getUsedCount()).isEqualTo(3);
    }
}
