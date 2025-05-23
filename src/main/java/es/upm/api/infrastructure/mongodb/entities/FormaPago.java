package es.upm.api.infrastructure.mongodb.entities;

import es.upm.api.domain.model.FormaPagos;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormaPago {
    private String descripcion;
    private Integer porcentaje;

    public FormaPago(FormaPagos formaPagos) {
        BeanUtils.copyProperties(formaPagos, this);
    }

    public FormaPagos toFormaPago() {
        FormaPagos formaPagos = new FormaPagos();
        BeanUtils.copyProperties(this, formaPagos);
        return formaPagos;
    }
}

