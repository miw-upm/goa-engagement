package es.upm.api.adapter.in.resources;

import es.upm.api.domain.model.LegalTask;
import es.upm.api.domain.services.LegalTaskService;
import es.upm.miw.security.Security;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@PreAuthorize(Security.ADMIN_MANAGER_OPERATOR)
@RestController
@RequestMapping(LegalTaskResource.LEGAL_TASKS)
@RequiredArgsConstructor
public class LegalTaskResource {
    public static final String LEGAL_TASKS = "/legal-tasks";
    public static final String ID_ID = "/{id}";

    private final LegalTaskService legalTaskService;

    @PostMapping
    public void create(@Valid @RequestBody LegalTask legalTask) {
        this.legalTaskService.create(legalTask);
    }

    @GetMapping(ID_ID)
    public LegalTask read(@PathVariable UUID id) {
        return this.legalTaskService.read(id);
    }

    @PutMapping(ID_ID)
    public void update(@PathVariable UUID id, @RequestBody LegalTask legalTask) {
        this.legalTaskService.update(id, legalTask);
    }

    @PreAuthorize(Security.ADMIN)
    @DeleteMapping(ID_ID)
    public void delete(@PathVariable UUID id) {
        this.legalTaskService.deleteById(id);
    }

    @GetMapping
    public List<LegalTask> find(@RequestParam(required = false) String title) {
        return this.legalTaskService.find(title).toList();
    }

}
