package es.upm.api.infrastructure.mongodb.entities;

import es.upm.api.domain.model.Ticket;
import es.upm.api.domain.model.UserDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document
public class TicketEntity {
    @Id
    private UUID id;
    private String urlToken;
    private LocalDateTime creationDate;
    private List<TicketLineEntity> ticketLineEntities;
    private BigDecimal cash;
    private BigDecimal card;
    private BigDecimal voucher;
    private String note;
    private UUID userId;

    public TicketEntity(Ticket ticket) {
        BeanUtils.copyProperties(ticket, this);
        if (Objects.nonNull(ticket.getUserDto())) {
            this.userId = ticket.getUserDto().getId();
        }
        this.ticketLineEntities = ticket.getTicketLines().stream()
                .map(TicketLineEntity::new)
                .toList();
    }

    public Ticket toTicket() {
        Ticket ticket = new Ticket();
        BeanUtils.copyProperties(this, ticket);
        ticket.setTicketLines(this.getTicketLineEntities().stream()
                .map(TicketLineEntity::toTicketLine)
                .toList()
        );
        if (Objects.nonNull(this.userId)) {
            ticket.setUserDto(UserDto.builder().id(this.getUserId()).build());
        }
        return ticket;
    }

}
