package es.upm.api.infrastructure.resources;

import es.upm.api.domain.model.EngagementLetter;
import es.upm.api.domain.model.criteria.EngagementLetterFindCriteria;
import es.upm.api.domain.services.EngagementLetterService;
import es.upm.miw.security.Security;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@PreAuthorize(Security.ADMIN_MANAGER_OPERATOR)
@RequestMapping(EngagementLetterResource.ENGAGEMENT_LETTER)
@RequiredArgsConstructor
public class EngagementLetterResource {
    public static final String ENGAGEMENT_LETTER = "/engagement-letters";
    public static final String ID_ID = "/{id}";
    public static final String PUBLIC_ACCESS_TOKEN = "/public-access-token";
    public static final String PRINT_VIEW = "/print-view";

    private final EngagementLetterService engagementLetterService;

    @GetMapping
    public List<EngagementLetter> searchNullSafe(@ModelAttribute EngagementLetterFindCriteria criteria) {
        return this.engagementLetterService.searchNullSafe(criteria).toList();
    }

    @PostMapping
    public void create(@Valid @RequestBody EngagementLetter engagementLetter) {
        this.engagementLetterService.create(engagementLetter);
    }

    @GetMapping(ID_ID)
    public EngagementLetter read(@PathVariable UUID id) {
        return this.engagementLetterService.readById(id);
    }

    @GetMapping(value = ID_ID + PRINT_VIEW, produces = {"application/pdf", "application/json"})
    public byte[] readPrintView(@PathVariable UUID id) {
        return this.engagementLetterService.generatePdf(id);
    }

    @PutMapping(ID_ID)
    public void update(@PathVariable UUID id, @Valid @RequestBody EngagementLetter engagementLetter) {
        this.engagementLetterService.update(id, engagementLetter);
    }

    @PreAuthorize(Security.ADMIN)
    @DeleteMapping(ID_ID)
    public void delete(@PathVariable UUID id) {
        this.engagementLetterService.delete(id);
    }

}

