package es.upm.api.domain.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import es.upm.api.domain.model.validations.ListNotEmpty;
import es.upm.api.domain.model.validations.PositiveBigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {
    private UUID id;
    private String urlToken;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime creationDate;
    @ListNotEmpty
    private List<TicketLine> ticketLines;
    @PositiveBigDecimal
    private BigDecimal cash;
    @PositiveBigDecimal
    private BigDecimal card;
    @PositiveBigDecimal
    private BigDecimal voucher;
    @NotNull
    @NotBlank
    private String note;
    private UserDto userDto;

    public BigDecimal total() {
        return this.ticketLines.stream()
                .map(TicketLine::totalShopping)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal pay() {
        return this.cash.add(this.card).add(this.voucher);
    }

    public BigDecimal debt() {
        return this.total().subtract(this.pay());
    }

    public boolean hasDebt() {
        return this.pay().compareTo(this.total()) < 0;
    }

    public int itemsNotCommitted() {
        return this.getTicketLines().stream()
                .filter(ticketLine -> LineState.NOT_COMMITTED.equals(ticketLine.getState()))
                .mapToInt(TicketLine::getAmount)
                .sum();
    }

}
