package es.upm.api.infrastructure.mongodb.entities;

import es.upm.api.domain.model.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodEntity {
    private String description;
    private Integer percentage;

    public PaymentMethodEntity(PaymentMethod paymentMethod) {
        BeanUtils.copyProperties(paymentMethod, this);
    }

    public PaymentMethod toPaymentMethod() {
        PaymentMethod paymentMethod = new PaymentMethod();
        BeanUtils.copyProperties(this, paymentMethod);
        return paymentMethod;
    }
}

