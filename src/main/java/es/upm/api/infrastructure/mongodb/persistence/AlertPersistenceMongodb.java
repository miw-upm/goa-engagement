package es.upm.api.infrastructure.mongodb.persistence;

import es.upm.api.domain.exceptions.NotFoundException;
import es.upm.api.domain.model.Alert;
import es.upm.api.domain.persistence.AlertPersistence;
import es.upm.api.infrastructure.mongodb.entities.AlertEntity;
import es.upm.api.infrastructure.mongodb.repositories.AlertRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class AlertPersistenceMongodb implements AlertPersistence {

    private final AlertRepository alertRepository;

    public AlertPersistenceMongodb(AlertRepository repository) {
        this.alertRepository = repository;
    }

    @Override
    public void create(Alert alert) {
        AlertEntity alertEntity = new AlertEntity(alert);
        this.alertRepository.save(alertEntity);
    }

    @Override
    public Alert readById(UUID id) {
        return this.alertRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("The Alert ID doesn't exist: " + id))
                .toAlert();
    }

    @Override
    public void update(Alert alert) {
        AlertEntity alertEntity = new AlertEntity(alert);
        this.alertRepository.save(alertEntity);
    }

    @Override
    public List<Alert> findByEngagementLetterId(UUID engagementLetterId) {
        return this.alertRepository.findByEngagementLetterId(engagementLetterId).stream()
                .map(AlertEntity::toAlert)
                .toList();
    }
}
