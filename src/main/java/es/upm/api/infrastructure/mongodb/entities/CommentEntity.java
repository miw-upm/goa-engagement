package es.upm.api.infrastructure.mongodb.entities;

import es.upm.api.domain.model.Comment;
import lombok.*;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentEntity {
    private UUID authorId;
    private LocalDateTime createdDate;
    private String content;

    public CommentEntity(Comment comment) {
        BeanUtils.copyProperties(comment, this);
        this.authorId = Objects.requireNonNull(comment.getAuthorId(), "Comment author ID is required");
        Objects.requireNonNull(this.authorId, "Comment author ID is required");
    }

    public Comment toComment() {
        Objects.requireNonNull(this.authorId, "Comment author ID is required");
        Comment comment = new Comment();
        BeanUtils.copyProperties(this, comment);
        return comment;
    }
}
