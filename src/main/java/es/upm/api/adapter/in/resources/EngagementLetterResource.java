package es.upm.api.adapter.in.resources;

import es.upm.api.domain.model.AcceptanceEngagement;
import es.upm.api.domain.model.EngagementLetter;
import es.upm.api.domain.model.criteria.EngagementLetterFindCriteria;
import es.upm.api.domain.model.external.UserSnapshot;
import es.upm.api.domain.services.EngagementLetterService;
import es.upm.miw.device.DeviceInfoResolver;
import es.upm.miw.security.Security;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@PreAuthorize(Security.ADMIN_MANAGER_OPERATOR)
@RestController
@RequestMapping(EngagementLetterResource.ENGAGEMENT_LETTER)
@RequiredArgsConstructor
public class EngagementLetterResource {
    public static final String ENGAGEMENT_LETTER = "/engagement-letters";
    public static final String ID_ID = "/{id}";
    public static final String READ_ENGAGEMENT_LETTER = "/read-engagement-letter";
    public static final String PENDING_SIGNERS = "/pending-signers";
    public static final String SIGN_ENGAGEMENT_LETTER = "/sign-engagement-letter";
    public static final String URL_ID_TOKEN_ID = "/{urlId}/{token}";
    public static final String VIEW = "/view";

    private final EngagementLetterService engagementLetterService;

    @PostMapping
    public void create(@Valid @RequestBody EngagementLetter engagementLetter) {
        this.engagementLetterService.create(engagementLetter);
    }

    @GetMapping(ID_ID)
    public EngagementLetter readById(@PathVariable UUID id) {
        return this.engagementLetterService.readById(id);
    }

    @GetMapping(value = ID_ID + VIEW, produces = MediaType.APPLICATION_PDF_VALUE)
    public byte[] readPdf(@PathVariable UUID id) {
        return this.engagementLetterService.generatePdf(id);
    }

    @PreAuthorize(Security.ADMIN)
    @PutMapping(ID_ID)
    public void update(@PathVariable UUID id, @Valid @RequestBody EngagementLetter engagementLetter) {
        this.engagementLetterService.update(id, engagementLetter);
    }

    @PreAuthorize(Security.ADMIN)
    @DeleteMapping(ID_ID)
    public void delete(@PathVariable UUID id) {
        this.engagementLetterService.delete(id);
    }

    @GetMapping
    public List<EngagementLetter> find(@ModelAttribute EngagementLetterFindCriteria criteria) {
        return this.engagementLetterService.find(criteria).toList();
    }

    @GetMapping(ID_ID + PENDING_SIGNERS)
    public List<UserSnapshot> findPendingSigners(@PathVariable UUID id) {
        return this.engagementLetterService.findPendingSigners(id).toList();
    }

    @PreAuthorize(Security.ALL)
    @GetMapping(value = READ_ENGAGEMENT_LETTER + URL_ID_TOKEN_ID, produces = MediaType.APPLICATION_PDF_VALUE)
    public byte[] readPdfWithToken(@PathVariable String urlId, @PathVariable String token) {
        return this.engagementLetterService.readPdfWithToken(READ_ENGAGEMENT_LETTER.substring(1), urlId, token);
    }

    @PreAuthorize(Security.ALL)
    @GetMapping(value = SIGN_ENGAGEMENT_LETTER + URL_ID_TOKEN_ID, produces = MediaType.APPLICATION_PDF_VALUE)
    public byte[] readBeforeSigningWithToken(@PathVariable String urlId, @PathVariable String token) {
        return this.engagementLetterService.readPdfWithToken(SIGN_ENGAGEMENT_LETTER.substring(1), urlId, token);
    }

    @PreAuthorize(Security.ALL)
    @PatchMapping(value = SIGN_ENGAGEMENT_LETTER + URL_ID_TOKEN_ID)
    public void signWithToken(@PathVariable String urlId, @PathVariable String token,
                              @RequestBody AcceptanceEngagementCreationDto acceptanceCreation,
                              HttpServletRequest request) {
        AcceptanceEngagement acceptance = AcceptanceEngagement.builder()
                .signatureToken(token)
                .documentAccepted(acceptanceCreation.getDocumentAccepted())
                .deviceInfo(DeviceInfoResolver.resolve(request))
                .build();
        this.engagementLetterService.signWithToken(SIGN_ENGAGEMENT_LETTER.substring(1), urlId, acceptance);
    }
}
