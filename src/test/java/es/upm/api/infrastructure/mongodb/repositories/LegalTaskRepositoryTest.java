package es.upm.api.infrastructure.mongodb.repositories;

import es.upm.api.infrastructure.mongodb.entities.LegalTaskEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class LegalTaskRepositoryTest {

    @Autowired
    private LegalTaskRepository legalTaskRepository;

    @Test
    void testFindNullSafeWhenTituloIsNull() {
        List<LegalTaskEntity> result = legalTaskRepository.findByTitleContainingIgnoreCase("jur", Sort.by(Sort.Direction.ASC, "titulo"));

        assertThat(result)
                .extracting(LegalTaskEntity::getTitle)
                .contains("Asesoramiento jurídico");
    }
}
