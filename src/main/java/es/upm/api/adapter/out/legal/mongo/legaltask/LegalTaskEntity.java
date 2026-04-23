package es.upm.api.adapter.out.legal.mongo.legaltask;

import es.upm.api.domain.model.LegalTask;
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
public class LegalTaskEntity {
    @Id
    private UUID id;
    @Indexed(unique = true)
    private String title;

    public LegalTaskEntity(LegalTask legalTask) {
        BeanUtils.copyProperties(legalTask, this);
    }

    public LegalTask toDomain() {
        LegalTask legalTask = new LegalTask();
        BeanUtils.copyProperties(this, legalTask);
        return legalTask;
    }
}

