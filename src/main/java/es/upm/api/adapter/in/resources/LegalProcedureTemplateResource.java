package es.upm.api.adapter.in.resources;

import es.upm.api.domain.model.LegalProcedureTemplate;
import es.upm.api.domain.model.criteria.LegalProcedureTemplateFindCriteria;
import es.upm.api.domain.services.LegalProcedureTemplateService;
import es.upm.miw.security.Security;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@PreAuthorize(Security.ADMIN_MANAGER_OPERATOR)
@RequestMapping(LegalProcedureTemplateResource.LEGAL_PROCEDURE_TEMPLATE)
@RequiredArgsConstructor
public class LegalProcedureTemplateResource {
    public static final String LEGAL_PROCEDURE_TEMPLATE = "/legal-procedure-templates";
    public static final String ID_ID = "/{id}";

    private final LegalProcedureTemplateService legalProcedureTemplateService;

    @PostMapping
    public void create(@Valid @RequestBody LegalProcedureTemplate template) {
        this.legalProcedureTemplateService.create(template);
    }

    @GetMapping(ID_ID)
    public LegalProcedureTemplate read(@PathVariable UUID id) {
        return this.legalProcedureTemplateService.readById(id);
    }

    @PutMapping(ID_ID)
    public void update(@PathVariable UUID id, @Valid @RequestBody LegalProcedureTemplate template) {
        this.legalProcedureTemplateService.update(id, template);
    }

    @PreAuthorize(Security.ADMIN)
    @DeleteMapping(ID_ID)
    public void delete(@PathVariable UUID id) {
        this.legalProcedureTemplateService.delete(id);
    }

    @GetMapping
    public List<LegalProcedureTemplate> find(@ModelAttribute LegalProcedureTemplateFindCriteria criteria) {
        return this.legalProcedureTemplateService.find(criteria).toList();
    }
}

