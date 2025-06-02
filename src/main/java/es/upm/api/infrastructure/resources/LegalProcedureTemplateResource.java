package es.upm.api.infrastructure.resources;

import es.upm.api.domain.model.LegalProcedureTemplate;
import es.upm.api.domain.services.LegalProcedureTemplateService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.stream.Stream;

@RestController
@PreAuthorize(Security.ADMIN_MANAGER_OPERATOR)
@RequestMapping(LegalProcedureTemplateResource.LEGAL_PROCEDURE_TEMPLATE)
public class LegalProcedureTemplateResource {
    public static final String LEGAL_PROCEDURE_TEMPLATE = "/legal-procedure-templates";
    public static final String ID_ID = "/{id}";

    private final LegalProcedureTemplateService legalProcedureTemplateService;

    @Autowired
    public LegalProcedureTemplateResource(LegalProcedureTemplateService legalProcedureTemplateService) {
        this.legalProcedureTemplateService = legalProcedureTemplateService;
    }

    @GetMapping
    public Stream<LegalProcedureTemplate> findNullSafe(@RequestParam(required = false) String nombre) {
        return this.legalProcedureTemplateService.findNullSafe(nombre);
    }

    @GetMapping(ID_ID)
    public LegalProcedureTemplate read(@PathVariable UUID id) {
        return this.legalProcedureTemplateService.readById(id);
    }

    @PostMapping
    public void create(@Valid @RequestBody LegalProcedureTemplate legalProcedureTemplate) {
        this.legalProcedureTemplateService.create(legalProcedureTemplate);
    }

    @PutMapping(ID_ID)
    public void update(@PathVariable UUID id, @Valid @RequestBody LegalProcedureTemplate legalProcedureTemplate) {
        this.legalProcedureTemplateService.update(id, legalProcedureTemplate);
    }

    @DeleteMapping(ID_ID)
    public void delete(@PathVariable UUID id) {
        this.legalProcedureTemplateService.delete(id);
    }
}

