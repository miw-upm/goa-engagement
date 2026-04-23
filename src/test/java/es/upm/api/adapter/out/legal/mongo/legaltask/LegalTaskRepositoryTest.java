package es.upm.api.adapter.out.legal.mongo.legaltask;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class LegalTaskRepositoryTest {

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
