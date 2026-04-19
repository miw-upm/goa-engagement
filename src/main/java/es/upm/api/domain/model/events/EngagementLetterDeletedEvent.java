package es.upm.api.domain.model.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

/**
 * Evento publicado cuando se elimina un EngagementLetter.
 * Permite que otros servicios reaccionen al evento sin acoplamiento directo.
 */
@Getter
public class EngagementLetterDeletedEvent extends ApplicationEvent {
    private final UUID engagementLetterId;

    public EngagementLetterDeletedEvent(Object source, UUID engagementLetterId) {
        super(source);
        this.engagementLetterId = engagementLetterId;
    }

}

