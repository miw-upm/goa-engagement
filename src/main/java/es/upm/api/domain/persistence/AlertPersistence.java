package es.upm.api.domain.persistence;

import es.upm.api.domain.model.Alert;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AlertPersistence {
    void create(Alert alert);

    Alert readById(UUID id);
}
