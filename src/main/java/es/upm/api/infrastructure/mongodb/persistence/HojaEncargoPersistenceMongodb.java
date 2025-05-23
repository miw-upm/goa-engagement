package es.upm.api.infrastructure.mongodb.persistence;

import es.upm.api.domain.exceptions.NotFoundException;
import es.upm.api.domain.model.HojaEncargo;
import es.upm.api.domain.persistence.HojaEncargoPersistence;
import es.upm.api.infrastructure.mongodb.repositories.HojaEncargoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class HojaEncargoPersistenceMongodb implements HojaEncargoPersistence {

    private final HojaEncargoRepository hojaEncargoRepository;

    @Autowired
    public HojaEncargoPersistenceMongodb(HojaEncargoRepository hojaEncargoRepository) {
        this.hojaEncargoRepository = hojaEncargoRepository;
    }

    @Override
    public HojaEncargo readById(UUID id) {
        return this.hojaEncargoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("The HojaEncargo ID doesn't exist: " + id))
                .toHojaEncargo();
    }
}

