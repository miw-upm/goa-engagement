package es.upm.api.adapter.in.resources;

import es.upm.api.domain.model.LegalProcedureTemplate;
import es.upm.api.domain.model.criteria.LegalProcedureTemplateFindCriteria;
import es.upm.api.domain.services.LegalProcedureTemplateService;
import es.upm.miw.security.Security;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@PreAuthorize(Security.ADMIN_MANAGER)
@RestController
@RequestMapping(LegalProcedureTemplateResource.LEGAL_PROCEDURE_TEMPLATE)
@RequiredArgsConstructor
public class LegalProcedureTemplateResource {
    public static final String LEGAL_PROCEDURE_TEMPLATE = "/legal-procedure-templates";
    public static final String ID_ID = "/{id}";

    private final LegalProcedureTemplateService legalProcedureTemplateService;

    @PreAuthorize(Security.ADMIN_MANAGER_LAWYER_OPERATOR)
    @PostMapping
    public void create(@Valid @RequestBody LegalProcedureTemplate template) {
        this.legalProcedureTemplateService.create(template);
    }

    @PreAuthorize(Security.ADMIN_MANAGER_LAWYER_OPERATOR)
    @GetMapping(ID_ID)
    public LegalProcedureTemplate read(@PathVariable UUID id, Authentication authentication) {
        LegalProcedureTemplate template = this.legalProcedureTemplateService.readById(id);
        if (this.hasLawyerRole(authentication)) {
            template.setBudget(null);
        }
        return template;
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

    @PreAuthorize(Security.ADMIN_MANAGER_LAWYER_OPERATOR)
    @GetMapping
    public List<LegalProcedureTemplate> find(@ModelAttribute LegalProcedureTemplateFindCriteria criteria,
                                             Authentication authentication) {
        List<LegalProcedureTemplate> templates = this.legalProcedureTemplateService.find(criteria).toList();
        if (this.hasLawyerRole(authentication)) {
            templates.forEach(template -> template.setBudget(null));
        }
        return templates;
    }

    private boolean hasLawyerRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_lawyer".equals(authority.getAuthority()));
    }
}

