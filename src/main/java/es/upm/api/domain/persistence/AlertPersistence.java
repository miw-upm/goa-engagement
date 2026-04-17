package es.upm.api.domain.persistence;

import es.upm.api.domain.model.Alert;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AlertPersistence {
    void create(Alert alert);

    Alert readById(UUID id);

    void update(Alert alert);

    List<Alert> findAll();

    List<Alert> findByEngagementLetterId(UUID engagementLetterId);
}
