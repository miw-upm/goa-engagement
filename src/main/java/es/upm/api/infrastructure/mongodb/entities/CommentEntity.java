package es.upm.api.infrastructure.mongodb.entities;

import es.upm.api.domain.model.Comment;
import es.upm.api.domain.model.UserDto;
import lombok.*;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;
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
        if (comment.getAuthor() != null) {
            this.authorId = comment.getAuthor().getId();
        }
    }

    public Comment toComment() {
        Comment comment = new Comment();
        BeanUtils.copyProperties(this, comment);
        if (this.authorId != null) {
            comment.setAuthor(UserDto.builder().id(this.authorId).build());
        }
        return comment;
    }
}
