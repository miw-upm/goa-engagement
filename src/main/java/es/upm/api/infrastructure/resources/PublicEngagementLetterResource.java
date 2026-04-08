package es.upm.api.infrastructure.resources;

import es.upm.api.domain.services.EngagementLetterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(PublicEngagementLetterResource.PUBLIC_ENGAGEMENT_LETTERS)
public class PublicEngagementLetterResource {
    public static final String PUBLIC_ENGAGEMENT_LETTERS = "/public/engagement-letters";
    public static final String ACCESS = "/access";

    private final EngagementLetterService engagementLetterService;

    @Autowired
    public PublicEngagementLetterResource(EngagementLetterService engagementLetterService) {
        this.engagementLetterService = engagementLetterService;
    }

    @GetMapping(ACCESS)
    public PublicEngagementLetterResponse readByToken(@RequestParam String token) {
        return new PublicEngagementLetterResponse(this.engagementLetterService.readPublicByToken(token));
    }
}
