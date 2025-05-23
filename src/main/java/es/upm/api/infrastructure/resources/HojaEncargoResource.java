package es.upm.api.infrastructure.resources;

import es.upm.api.domain.model.HojaEncargo;
import es.upm.api.domain.services.HojaEncargoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@PreAuthorize(Security.ADMIN_MANAGER_OPERATOR)
@RequestMapping(HojaEncargoResource.HOJAS_ENCARGO)
public class HojaEncargoResource {
    public static final String HOJAS_ENCARGO = "/hojas-encargo";
    public static final String ID_ID = "/{id}";

    private final HojaEncargoService hojaEncargoService;

    @Autowired
    public HojaEncargoResource(HojaEncargoService hojaEncargoService) {
        this.hojaEncargoService = hojaEncargoService;
    }

    @PreAuthorize(Security.ALL)
    @GetMapping(ID_ID)
    public HojaEncargo read(@PathVariable UUID id) {
        return this.hojaEncargoService.readById(id);
    }
}

