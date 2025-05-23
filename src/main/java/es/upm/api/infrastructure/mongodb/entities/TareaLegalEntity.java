package es.upm.api.infrastructure.mongodb.entities;

import es.upm.api.domain.model.TareaLegal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document
public class TareaLegalEntity {
    @Id
    private UUID id;
    @Indexed(unique = true)
    private String titulo;

    public TareaLegalEntity(TareaLegal tareaLegal) {
        BeanUtils.copyProperties(tareaLegal, this);
    }

    public TareaLegal toTareaLegal() {
        TareaLegal tareaLegal = new TareaLegal();
        BeanUtils.copyProperties(this, tareaLegal);
        return tareaLegal;
    }
}

