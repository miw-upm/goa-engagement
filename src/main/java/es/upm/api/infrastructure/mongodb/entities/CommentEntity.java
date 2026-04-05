package es.upm.api.infrastructure.mongodb.entities;

import es.upm.api.domain.model.Comment;
import lombok.*;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentEntity {
    private LocalDateTime date;
    private String content;

    public CommentEntity(Comment comment) {
        BeanUtils.copyProperties(comment, this);
    }

    public Comment toComment() {
        Comment comment = new Comment();
        BeanUtils.copyProperties(this, comment);
        return comment;
    }
}
