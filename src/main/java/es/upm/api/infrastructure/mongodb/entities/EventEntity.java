package es.upm.api.infrastructure.mongodb.entities;

import es.upm.api.domain.model.Event;
import es.upm.api.domain.model.EventType;
import es.upm.api.domain.model.Status;
import lombok.*;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document
public class EventEntity {
    @Id
    private UUID id;
    private LocalDateTime date;
    private EventType type;
    private String title;
    private String description;
    private Status status;
    private UUID engagementLetterId;
    private List<CommentEntity> comments;

    public EventEntity(Event event) {
        BeanUtils.copyProperties(event, this);
        if(event.getComments() != null) {
            this.comments = event.getComments().stream().
                    map(CommentEntity::new).
                    toList();
        }
    }

    public Event toEvent() {
        Event event = new Event();
        BeanUtils.copyProperties(this, event);
        if(this.comments != null) {
            event.setComments(this.comments.stream()
                    .map(CommentEntity::toComment)
                    .toList());
        }
        return event;
    }

}
