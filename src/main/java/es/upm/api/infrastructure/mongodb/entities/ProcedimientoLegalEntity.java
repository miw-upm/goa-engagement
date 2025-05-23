package es.upm.api.infrastructure.mongodb.entities;

import es.upm.api.domain.model.ProcedimientoLegal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document
public class ProcedimientoLegalEntity {
    @Id
    private UUID id;
    @Indexed(unique = true)
    private String titulo;
    private LocalDate fechaInicio;
    private LocalDate fechaCierre;
    private BigDecimal presupuesto;
    private Boolean ivaIncluido;
    private List<String> tareasLegales;

    public ProcedimientoLegalEntity(ProcedimientoLegal procedimientoLegal) {
        BeanUtils.copyProperties(procedimientoLegal, this);
    }

    public ProcedimientoLegal toProcedimientoLegal() {
        ProcedimientoLegal procedimientoLegal = new ProcedimientoLegal();
        BeanUtils.copyProperties(this, procedimientoLegal);
        return procedimientoLegal;
    }
}

