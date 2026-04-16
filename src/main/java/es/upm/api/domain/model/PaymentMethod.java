package es.upm.api.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethod {
    private String description;
    private String percentage;

    @Override
    public String toString() {
        return percentage + " -- " + description;
    }

}
