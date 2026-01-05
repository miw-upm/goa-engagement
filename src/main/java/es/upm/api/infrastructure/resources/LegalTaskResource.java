package es.upm.api.infrastructure.resources;

import es.upm.api.domain.model.LegalTask;
import es.upm.api.domain.services.LegalTaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@PreAuthorize(Security.ADMIN_MANAGER_OPERATOR)
@RequestMapping(LegalTaskResource.LEGAL_TASKS)
public class LegalTaskResource {
    public static final String LEGAL_TASKS = "/legal-tasks";
    public static final String ID_ID = "/{id}";

    private final LegalTaskService legalTaskService;

    @Autowired
    public LegalTaskResource(LegalTaskService legalTaskService) {
        this.legalTaskService = legalTaskService;
    }

    @GetMapping(ID_ID)
    public LegalTask read(@PathVariable UUID id) {
        return this.legalTaskService.read(id);
    }

    @GetMapping
    public List<LegalTask> findNullSafe(@RequestParam(required = false) String title) {
        return this.legalTaskService.findNullSafe(title).toList();
    }

    @PostMapping
    public void create(@Valid @RequestBody LegalTask legalTask) {
        this.legalTaskService.create(legalTask);
    }

    @DeleteMapping(ID_ID)
    public void deleteByID(@PathVariable UUID id) {
        this.legalTaskService.deleteById(id);
    }

    @PutMapping(ID_ID)
    public void update(@PathVariable UUID id, @RequestBody LegalTask legalTask) {
        this.legalTaskService.update(id, legalTask);
    }

}
