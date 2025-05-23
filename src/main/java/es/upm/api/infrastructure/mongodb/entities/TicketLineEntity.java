package es.upm.api.infrastructure.mongodb.entities;

import es.upm.api.domain.model.ArticleDto;
import es.upm.api.domain.model.LineState;
import es.upm.api.domain.model.TicketLine;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketLineEntity {
    private UUID articleId;
    private BigDecimal retailPrice;
    private Integer amount;
    private BigDecimal discount;
    private LineState state;

    public TicketLineEntity(TicketLine ticketLine) {
        BeanUtils.copyProperties(ticketLine, this);
        this.articleId = ticketLine.getArticleDto().getId();
    }

    public TicketLine toTicketLine() {
        TicketLine ticketLine = new TicketLine();
        BeanUtils.copyProperties(this, ticketLine);
        ticketLine.setArticleDto(ArticleDto.builder().id(this.getArticleId()).build());
        return ticketLine;
    }

}
