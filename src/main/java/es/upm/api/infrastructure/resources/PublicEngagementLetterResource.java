package es.upm.api.infrastructure.resources;

import es.upm.api.domain.services.EngagementLetterService;
import es.upm.api.infrastructure.dtos.PublicEngagementLetterAcceptRequestDto;
import es.upm.api.infrastructure.dtos.PublicEngagementLetterAcceptResponseDto;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(PublicEngagementLetterResource.PUBLIC_ENGAGEMENT_LETTERS)
public class PublicEngagementLetterResource {
    public static final String PUBLIC_ENGAGEMENT_LETTERS = "/public/engagement-letters";
    public static final String ACCESS = "/access";
    public static final String ACCEPT = "/accept";

    private final EngagementLetterService engagementLetterService;

    @Autowired
    public PublicEngagementLetterResource(EngagementLetterService engagementLetterService) {
        this.engagementLetterService = engagementLetterService;
    }

    @GetMapping(ACCESS)
    public PublicEngagementLetterResponse readByToken(@RequestParam String token) {
        return new PublicEngagementLetterResponse(this.engagementLetterService.readPublicByToken(token));
    }

    @PostMapping(ACCEPT)
    public PublicEngagementLetterAcceptResponseDto accept(@Valid @RequestBody PublicEngagementLetterAcceptRequestDto requestDto) {
        return new PublicEngagementLetterAcceptResponseDto(this.engagementLetterService.acceptPublicByToken(requestDto.getToken()));
    }
}
