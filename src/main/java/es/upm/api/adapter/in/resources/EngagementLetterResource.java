package es.upm.api.adapter.in.resources;

import es.upm.api.domain.model.AcceptanceEngagement;
import es.upm.api.domain.model.EngagementLetter;
import es.upm.api.domain.model.criteria.EngagementLetterFindCriteria;
import es.upm.api.domain.model.external.UserSnapshot;
import es.upm.api.domain.services.EngagementLetterService;
import es.upm.miw.device.DeviceInfo;
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

@RestController
@PreAuthorize(Security.ADMIN_MANAGER_OPERATOR)
@RequestMapping(EngagementLetterResource.ENGAGEMENT_LETTER)
@RequiredArgsConstructor
public class EngagementLetterResource {
    public static final String ENGAGEMENT_LETTER = "/engagement-letters";
    public static final String ID_ID = "/{id}";
    public static final String VIEW = "/view";
    public static final String PENDING_SIGNERS = "/pending-signers";
    public static final String MOBILE_ID_TOKEN_ID = "/{mobile}/{token}";
    public static final String SIGN_ENGAGEMENT_LETTER = "/sign-engagement-letter";

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
    public byte[] readView(@PathVariable UUID id) {
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


    @GetMapping(ID_ID + PENDING_SIGNERS)
    public List<UserSnapshot> findPendingSigners(@PathVariable UUID id) {
        return this.engagementLetterService.findPendingSigners(id).toList();
    }

    @GetMapping
    public List<EngagementLetter> find(@ModelAttribute EngagementLetterFindCriteria criteria) {
        return this.engagementLetterService.find(criteria).toList();
    }

    @PreAuthorize(Security.ALL)
    @GetMapping(value = VIEW + MOBILE_ID_TOKEN_ID, produces = MediaType.APPLICATION_PDF_VALUE)
    public byte[] readViewWithToken(@PathVariable String mobile, @PathVariable String token) {
        return this.engagementLetterService.generatePdfWithToken(mobile, token);
    }

    @PreAuthorize(Security.ALL)
    @PatchMapping(value = SIGN_ENGAGEMENT_LETTER + MOBILE_ID_TOKEN_ID)
    public void signWithToken(@PathVariable String mobile, @PathVariable String token,
                              @RequestBody AcceptanceEngagementCreationDto acceptanceCreation,
                              HttpServletRequest request) {
        System.out.println(">SIGN>>>>>>>>>>>>>>Accepting engagement letter with mobile: " + mobile + " and token: " + token + " acceptance: " + acceptanceCreation);
        AcceptanceEngagement acceptance = AcceptanceEngagement.builder()
                .mobile(mobile)
                .signatureToken(token)
                .documentAccepted(acceptanceCreation.getDocumentAccepted())
                .signature(acceptanceCreation.getSignature())
                .deviceInfo(this.resolveDeviceInfo(request))
                .build();
        this.engagementLetterService.sign(acceptance);
    }

    private DeviceInfo resolveDeviceInfo(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        String xRealIp = request.getHeader("X-Real-IP");
        String ip;
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            ip = xForwardedFor.split(",")[0].trim();
        } else if (xRealIp != null && !xRealIp.isBlank()) {
            ip = xRealIp.trim();
        } else {
            ip = request.getRemoteAddr();
        }
        return DeviceInfoResolver.resolve(request.getHeader("User-Agent"), ip);
    }

}

