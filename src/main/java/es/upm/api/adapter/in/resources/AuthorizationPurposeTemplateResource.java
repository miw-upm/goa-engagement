package es.upm.api.adapter.in.resources;

import es.upm.api.domain.model.AuthorizationPurposeTemplate;
import es.upm.api.domain.services.AuthorizationPurposeTemplateService;
import es.upm.miw.security.Security;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@PreAuthorize(Security.ADMIN_MANAGER_OPERATOR)
@RestController
@RequestMapping(AuthorizationPurposeTemplateResource.AUTHORIZATION_PURPOSE_TEMPLATES)
@RequiredArgsConstructor
public class AuthorizationPurposeTemplateResource {
    public static final String AUTHORIZATION_PURPOSE_TEMPLATES = "/authorization-purpose-templates";
    public static final String ID_ID = "/{id}";

    private final AuthorizationPurposeTemplateService authorizationPurposeTemplateService;

    @PostMapping
    public void create(@Valid @RequestBody AuthorizationPurposeTemplate authorizationPurposeTemplate) {
        this.authorizationPurposeTemplateService.create(authorizationPurposeTemplate);
    }

    @GetMapping(ID_ID)
    public AuthorizationPurposeTemplate read(@PathVariable UUID id) {
        return this.authorizationPurposeTemplateService.read(id);
    }

    @PutMapping(ID_ID)
    public void update(@PathVariable UUID id, @RequestBody AuthorizationPurposeTemplate authorizationPurposeTemplate) {
        this.authorizationPurposeTemplateService.update(id, authorizationPurposeTemplate);
    }

    @PreAuthorize(Security.ADMIN)
    @DeleteMapping(ID_ID)
    public void delete(@PathVariable UUID id) {
        this.authorizationPurposeTemplateService.deleteById(id);
    }

    @GetMapping
    public List<AuthorizationPurposeTemplate> find(@RequestParam(required = false) String purpose) {
        return this.authorizationPurposeTemplateService.find(purpose).toList();
    }
}
