package es.upm.api.adapter.in.resources;

import es.upm.api.domain.model.EngagementLetter;
import es.upm.api.domain.model.criteria.EngagementLetterFindCriteria;
import es.upm.api.domain.model.external.UserSnapshot;
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
    public static final String PRINT_VIEW = "/print-view";
    public static final String PENDING_SIGNERS = ID_ID + "/pending-signers";
    public static final String VIEW_MOBILE_TOKEN = "/view/{mobile}/{token}";
    public static final String SIGN_ENGAGEMENT_LETTER_MOBILE_TOKEN = "/sign-engagement-letter/{mobile}/{token}";

    private final EngagementLetterService engagementLetterService;

    @PostMapping
    public void create(@Valid @RequestBody EngagementLetter engagementLetter) {
        this.engagementLetterService.create(engagementLetter);
    }

    @GetMapping(ID_ID)
    public EngagementLetter read(@PathVariable UUID id) {
        return this.engagementLetterService.readById(id);
    }

    @GetMapping(value = ID_ID + PRINT_VIEW, produces = {"application/pdf", "application/json"})
    public byte[] view(@PathVariable UUID id) {
        return this.engagementLetterService.generatePdf(id);
    }

    @PreAuthorize(Security.ALL)
    @GetMapping(value = VIEW_MOBILE_TOKEN, produces = {"application/pdf", "application/json"})
    public byte[] viewByToken(@PathVariable String mobile, @PathVariable String token) {
        System.out.println(">VIEW>>>>>>>>>>>>>>>>>> mobile: " + mobile + " token: " + token);
        return this.engagementLetterService.generatePdfWithToken(mobile, token);
    }

    @PreAuthorize(Security.ALL)
    @PatchMapping(value = SIGN_ENGAGEMENT_LETTER_MOBILE_TOKEN)
    public void signByToken(@PathVariable String mobile, @PathVariable String token,
                            @RequestBody AcceptanceEngagementCreationDto acceptance) {
        System.out.println(">SIGN>>>>>>>>>>>>>>Accepting engagement letter with mobile: " + mobile + " and token: " + token + " acceptance: " + acceptance);
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


    @GetMapping(PENDING_SIGNERS)
    public List<UserSnapshot> findPendingSigners(@PathVariable UUID id) {
        return this.engagementLetterService.findPendingSigners(id).toList();
    }

    @GetMapping
    public List<EngagementLetter> find(@ModelAttribute EngagementLetterFindCriteria criteria) {
        return this.engagementLetterService.find(criteria).toList();
    }

}

