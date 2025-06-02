package es.upm.api.domain.services;

import es.upm.api.domain.model.EngagementLetter;
import es.upm.api.domain.persistence.EngagementLetterPersistence;
import es.upm.api.infrastructure.webclients.UserWebClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class EngagementLetterService {

    private final EngagementLetterPersistence engagementLetterPersistence;
    private final UserWebClient userWebClient;

    @Autowired
    public EngagementLetterService(EngagementLetterPersistence engagementLetterPersistence,
                                   UserWebClient userWebClient) {
        this.engagementLetterPersistence = engagementLetterPersistence;
        this.userWebClient = userWebClient;
    }

    public EngagementLetter readById(UUID id) {
        EngagementLetter engagementLetter = this.engagementLetterPersistence.readById(id);
        engagementLetter.setOwner(
                this.userWebClient.readUserById(engagementLetter.getOwner().getId())
        );
        engagementLetter.setAttachments(engagementLetter.getAttachments().stream()
                .map(userDto -> this.userWebClient.readUserById(userDto.getId()))
                .toList()
        );
        return engagementLetter;
    }

}