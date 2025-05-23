package es.upm.api.domain.model;

import es.upm.api.domain.model.validations.PositiveBigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketLine {
    @NotBlank
    private ArticleDto articleDto;
    private BigDecimal retailPrice;
    @NotNull
    private Integer amount;
    @PositiveBigDecimal
    private BigDecimal discount;
    @NotNull
    private LineState state;

    public BigDecimal totalUnit() {
        this.discount = discount.setScale(6, RoundingMode.HALF_UP);
        BigDecimal totalUnit = retailPrice.multiply(
                BigDecimal.ONE.subtract(this.discount.divide(new BigDecimal("100"), RoundingMode.HALF_UP)));
        return totalUnit.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal totalShopping() {
        return totalUnit().multiply(new BigDecimal(amount));
    }

}
