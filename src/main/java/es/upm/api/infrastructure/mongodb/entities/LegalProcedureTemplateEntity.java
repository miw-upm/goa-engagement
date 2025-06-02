package es.upm.api.infrastructure.mongodb.entities;

import es.upm.api.domain.model.LegalProcedureTemplate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
public class LegalProcedureTemplateEntity {
    @Id
    private UUID id;
    @Indexed(unique = true)
    private String title;
    private BigDecimal budget;
    @DBRef
    private List<LegalTaskEntity> legalTaskEntities;

    public LegalProcedureTemplateEntity(LegalProcedureTemplate legalProcedureTemplate) {
        BeanUtils.copyProperties(legalProcedureTemplate, this);
    }

    public LegalProcedureTemplate toLegalProcedureTemplate() {
        LegalProcedureTemplate legalProcedureTemplate = new LegalProcedureTemplate();
        BeanUtils.copyProperties(this, legalProcedureTemplate);
        return legalProcedureTemplate;
    }
}

