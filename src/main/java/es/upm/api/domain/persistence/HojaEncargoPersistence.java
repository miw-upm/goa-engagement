package es.upm.api.domain.persistence;

import es.upm.api.domain.model.HojaEncargo;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface HojaEncargoPersistence {
    HojaEncargo readById(UUID id);
}
