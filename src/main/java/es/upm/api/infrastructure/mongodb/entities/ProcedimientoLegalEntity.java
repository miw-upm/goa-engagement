package es.upm.api.infrastructure.mongodb.entities;

import es.upm.api.domain.model.ProcedimientoLegal;
import lombok.*;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
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
    private Boolean finalizado;
    private BigDecimal presupuesto;
    private Boolean ivaIncluido;
    @Singular
    @DBRef
    private List<TareaLegalEntity> tareaLegalEntities;

    public ProcedimientoLegalEntity(ProcedimientoLegal procedimientoLegal) {
        BeanUtils.copyProperties(procedimientoLegal, this);
    }

    public ProcedimientoLegal toProcedimientoLegal() {
        ProcedimientoLegal procedimientoLegal = new ProcedimientoLegal();
        BeanUtils.copyProperties(this, procedimientoLegal);
        procedimientoLegal.setTareas(
                this.tareaLegalEntities.stream()
                        .map(TareaLegalEntity::toTareaLegal)
                        .toList()
        );
        return procedimientoLegal;
    }
}

