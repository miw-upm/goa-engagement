package es.upm.api.infrastructure.resources;

import es.upm.api.domain.model.LegalTask;
import es.upm.api.domain.services.LegalTaskService;
import es.upm.api.infrastructure.resources.view.LegalTasksDto;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.stream.Stream;

@RestController
@PreAuthorize(Security.ADMIN_MANAGER_OPERATOR)
@RequestMapping(LegalTaskResource.LEGAL_TASKS)
public class LegalTaskResource {
    public static final String LEGAL_TASKS = "/legal-tasks";
    public static final String ID_ID = "/{id}";
    public static final String TITLES = "/titles";

    private final LegalTaskService legalTaskService;

    @Autowired
    public LegalTaskResource(LegalTaskService legalTaskService) {
        this.legalTaskService = legalTaskService;
    }

    @GetMapping
    public Stream<LegalTask> findNullSafe(@RequestParam(required = false) String titulo) {
        return this.legalTaskService.findNullSafe(titulo);
    }

    @GetMapping(TITLES)
    public LegalTasksDto findTitles() {
        return new LegalTasksDto(this.legalTaskService.findAll()
                .map(LegalTask::getTitle)
                .toList());
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
