package es.upm.api.infrastructure.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import es.upm.api.domain.model.EngagementLetter;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
public class PublicEngagementLetterAcceptResponseDto {
    private final UUID engagementLetterId;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private final LocalDateTime signatureDate;

    public PublicEngagementLetterAcceptResponseDto(EngagementLetter engagementLetter) {
        this.engagementLetterId = engagementLetter.getId();
        this.signatureDate = lastSignatureDate(engagementLetter);
    }

    private static LocalDateTime lastSignatureDate(EngagementLetter engagementLetter) {
        return java.util.Optional.ofNullable(engagementLetter.getAcceptanceEngagements())
                .stream()
                .flatMap(List::stream)
                .reduce((first, second) -> second)
                .map(acceptance -> acceptance.getSignatureDate())
                .orElse(null);
    }
}
