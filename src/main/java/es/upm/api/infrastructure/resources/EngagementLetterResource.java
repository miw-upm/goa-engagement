package es.upm.api.infrastructure.resources;

import es.upm.api.domain.model.EngagementLetter;
import es.upm.api.domain.model.EngagementLetterFindCriteria;
import es.upm.api.domain.services.EngagementLetterService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.stream.Stream;

@RestController
@PreAuthorize(Security.ADMIN_MANAGER_OPERATOR)
@RequestMapping(EngagementLetterResource.ENGAGEMENT_LETTER)
public class EngagementLetterResource {
    public static final String ENGAGEMENT_LETTER = "/engagement-letters";
    public static final String ID_ID = "/{id}";

    private final EngagementLetterService engagementLetterService;

    @Autowired
    public EngagementLetterResource(EngagementLetterService engagementLetterService) {
        this.engagementLetterService = engagementLetterService;
    }

    @GetMapping
    public Stream<EngagementLetter> findNullSafe(@ModelAttribute EngagementLetterFindCriteria criteria) {
       return this.engagementLetterService.findNullSafe(criteria);
    }

    @PostMapping
    public void create(@Valid @RequestBody EngagementLetter engagementLetter) {
        this.engagementLetterService.create(engagementLetter);
    }

    @GetMapping(ID_ID)
    public EngagementLetter read(@PathVariable UUID id) {
        return this.engagementLetterService.readById(id);
    }

    @PutMapping(ID_ID)
    public void update(@PathVariable UUID id, @Valid @RequestBody EngagementLetter engagementLetter) {
        this.engagementLetterService.update(id, engagementLetter);
    }

    @DeleteMapping(ID_ID)
    public void delete(@PathVariable UUID id) {
        this.engagementLetterService.delete(id);
    }

}

