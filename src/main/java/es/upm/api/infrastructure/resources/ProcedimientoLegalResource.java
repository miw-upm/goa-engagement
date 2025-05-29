package es.upm.api.infrastructure.resources;

import es.upm.api.domain.model.ProcedimientoLegal;
import es.upm.api.domain.services.ProcedimientoLegalService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.stream.Stream;

@RestController
@PreAuthorize(Security.ADMIN_MANAGER_OPERATOR)
@RequestMapping(ProcedimientoLegalResource.PROCEDIMIENTOS_LEGALES)
public class ProcedimientoLegalResource {
    public static final String PROCEDIMIENTOS_LEGALES = "/procedimientos-legales";
    public static final String ID_ID = "/{id}";

    private final ProcedimientoLegalService procedimientoLegalService;

    @Autowired
    public ProcedimientoLegalResource(ProcedimientoLegalService procedimientoLegalService) {
        this.procedimientoLegalService = procedimientoLegalService;
    }

    @GetMapping
    public Stream<ProcedimientoLegal> findNullSafe(@RequestParam(required = false) String nombre) {
        return this.procedimientoLegalService.findNullSafe(nombre);
    }

    @GetMapping(ID_ID)
    public ProcedimientoLegal read(@PathVariable UUID id) {
        return this.procedimientoLegalService.readById(id);
    }

    @PostMapping
    public void create(@Valid @RequestBody ProcedimientoLegal procedimientoLegal) {
        this.procedimientoLegalService.create(procedimientoLegal);
    }

    @PutMapping(ID_ID)
    public void update(@PathVariable UUID id, @Valid @RequestBody ProcedimientoLegal procedimientoLegal) {
        this.procedimientoLegalService.update(id, procedimientoLegal);
    }

    @DeleteMapping(ID_ID)
    public void delete(@PathVariable UUID id) {
        this.procedimientoLegalService.delete(id);
    }
}

