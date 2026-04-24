package es.upm.api.domain.ports.out.legal;

import es.upm.api.domain.model.LegalTask;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.stream.Stream;

@Repository
public interface LegalTaskGateway {
    void create(LegalTask legalTask);

    void deleteById(UUID id);

    LegalTask read(UUID id);

    Stream<LegalTask> find(String title);

    Stream<LegalTask> findAll();

    void update(UUID id, LegalTask legalTask);
}
